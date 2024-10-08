package co.uk.bittwisted;

import co.uk.bittwisted.config.AppConfig;
import co.uk.bittwisted.enums.Position;
import co.uk.bittwisted.util.Helpers;
import co.uk.bittwisted.views.InfoView;
import co.uk.bittwisted.views.SettingsView;
import com.formdev.flatlaf.FlatDarkLaf;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static javax.swing.SwingUtilities.*;

public class CapsLockHook extends JFrame implements NativeKeyListener {
    private Timer timer;
    private final Robot robot;
    private final Font defaultFont = new Font("Arial Black", Font.PLAIN, 50);
    private final Logger logger = Logger.getLogger(CapsLockHook.class.getName());
    private final SettingsView settingsView;
    private boolean capsLockOn;
    private boolean isSuccessPopup;
    private boolean RESET_IN_ACTION = false;

    private final Point topLeft;
    private final Point topRight;
    private final Point topCenter;
    private final Point bottomLeft;
    private final Point bottomRight;
    private final Point bottomCenter;

    private final AppConfig appConfig;
    private final String appDataFolderPath = System.getenv("APPDATA") + "\\cap-lock-hook";

    private final Color borderColor = new Color(187, 187, 187);
    private final GradientPaint defaultBackgroundGradient;

    public CapsLockHook() throws AWTException {
        appConfig = new AppConfig(appDataFolderPath);

        setTitle(appConfig.getAppName());
        setType(Type.UTILITY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(100, 100);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        robot = new Robot();
        defaultBackgroundGradient = new GradientPaint(
                100,
                0,
                new Color(60, 63, 65),
                getWidth()+50,
                getHeight(),
                new Color(78, 80, 82));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = ge.getMaximumWindowBounds();
        topLeft = new Point(bounds.x + 10, bounds.y + 10);
        topRight = new Point(bounds.x + bounds.width - getSize().width - 10, bounds.y + 10);
        topCenter = new Point(bounds.x + (bounds.width/2) - getSize().width/2, bounds.y + 10);
        bottomLeft = new Point(bounds.x + 10, bounds.y + bounds.height - getSize().height - 10);
        bottomRight = new Point(bounds.x + bounds.width - getSize().width - 10, bounds.y + bounds.height - getSize().height - 10);
        bottomCenter = new Point(bounds.x + (bounds.width/2) - getSize().width/2, bounds.y + bounds.height - getSize().height - 10);
        setLocation(bottomRight);

        startCapsLockHook();

        // get initial state of cap lock
        Toolkit kit = Toolkit.getDefaultToolkit();
        capsLockOn = kit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

        setupSystemTray();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopCapsLockHook));

        settingsView = new SettingsView(this, appConfig);
        InfoView infoView = new InfoView(settingsView, appConfig);
        settingsView.setInfoView(infoView);
        if(appConfig.isFirstTimeUser())  {
            settingsView.showSettings();
            infoView.showInfo();
        }

        updatePopupPosition(Position.valueOf(appConfig.getLocation()));
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            logger.log(Level.SEVERE, "System tray is not supported!");
        } else {
            SystemTray tray = SystemTray.getSystemTray();

            ImageIcon icon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemResource("icon_tray.png")));
            Image image = icon.getImage();

            PopupMenu popupMenu = new PopupMenu();
            MenuItem settingsItem = new MenuItem("Settings");
            settingsItem.addActionListener(e -> {
                settingsView.showSettings();
                logger.log(Level.INFO, "Settings");
            });
            popupMenu.add(settingsItem);
            MenuItem flipCapsLockItem = new MenuItem("Flip Cap Lock");
            flipCapsLockItem.addActionListener(e -> {
                flipCapLockState();
                showCapsLockStatusPopup();
                logger.log(Level.INFO, "Flip caps lock state");
            });
            popupMenu.add(flipCapsLockItem);
            popupMenu.addSeparator();
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            popupMenu.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, appConfig.getAppName(), popupMenu);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.log(Level.SEVERE, "TrayIcon could not be added.", e);
                return;
            }

            addWindowStateListener(new java.awt.event.WindowAdapter() {
                public void windowStateChanged(java.awt.event.WindowEvent evt) {
                    if (evt.getNewState() == JFrame.ICONIFIED) {
                        try {
                            setVisible(false);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Tray could not be minimized.", e);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        String message = capsLockOn ? "A" : "a";

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        g2d.setPaint(defaultBackgroundGradient);
        g2d.fillRect(0, 0, 100, 100);

        if(!isSuccessPopup) {
            if(RESET_IN_ACTION) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(borderColor);
            }
            g2d.setFont(defaultFont);
            g2d.drawString(message, 32, 65);
        } else {
            // Define the path of the check mark
            Path2D check = new Path2D.Double();
            check.moveTo(10 + 25, 30 + 20);
            check.lineTo(20 + 25, 40 + 20);
            check.lineTo(40 + 25, 20 + 20);

            // Draw the polygon
            g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(Color.GREEN);
            g2d.draw(check);
        }

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 30, 30));
        g2d.draw(new RoundRectangle2D.Double(5, 5, getWidth() - 10, getHeight() - 10, 27, 27));
    }

    private void flipCapLockState() {
        if(RESET_IN_ACTION) return;
        capsLockOn = !capsLockOn;

        RESET_IN_ACTION = true;
        Timer timer = new Timer(500, t -> {
            RESET_IN_ACTION = false;
        });
        timer.setRepeats(false);
        timer.start();

        repaint();

        logger.log(Level.INFO, "Reset caps lock");
    }

    public void stopCapsLockHook() {
        try {
            logger.log(Level.INFO, "Destroy hook");
            GlobalScreen.unregisterNativeHook();
            GlobalScreen.removeNativeKeyListener(this);
        } catch (NativeHookException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void startCapsLockHook() {
        try {
            logger.log(Level.INFO, "Start hook");
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            Logger.getLogger(CapsLockHook.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updatePopupPosition(Position position) {
        logger.log(Level.INFO, "POSITION:" + position);
        switch (position) {
            case TOP_LEFT: setLocation(topLeft); break;
            case TOP_RIGHT: setLocation(topRight); break;
            case TOP_CENTER: setLocation(topCenter); break;
            case BOTTOM_LEFT: setLocation(bottomLeft); break;
            case BOTTOM_RIGHT: setLocation(bottomRight); break;
            case BOTTOM_CENTER: setLocation(bottomCenter); break;
        }
        appConfig.updateLocation(position);
        showCapsLockStatusPopup();
    }

    public void updatePopupDelay(boolean isUp) {
        float popupDelay = Float.parseFloat(appConfig.getPopUpDelay());
        if(isUp) {
            if(popupDelay < 5f) {
                popupDelay += .5f;
            } else {
                popupDelay = 5f;
            }
        } else {
            if(popupDelay > 1f) {
                popupDelay -= .5f;
            } else {
                popupDelay = 1f;
            }
        }
        appConfig.updatePopUpDelay(Helpers.formatWithOneDecimalPlace(popupDelay));
        setVisible(false);
        showCapsLockStatusPopup();
    }

    public void showCapsLockStatusPopup() {
        isSuccessPopup = false;
        if (isVisible()) {
            logger.log(Level.SEVERE, "RESTART");
            timer.restart();
        } else {
            setVisible(true);
            hidePopup();
        }
        repaint();
    }

    private void showSuccessStatusPopup() {
        isSuccessPopup = true;
        if (isVisible()) {
            logger.log(Level.SEVERE, "RESTART");
            timer.restart();
        } else {
            setVisible(true);
            hidePopup();
        }
        repaint();
    }

    private void quickFixUpperCaseText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(""), null);

        // Simulate Ctrl+C to copy new text
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        try {
            sleep(50);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        String content = Helpers.getClipboardContentAsString();
        logger.log(Level.INFO,"Convert content:" + content);
        if(!content.isEmpty()) {
            StringSelection selection =
                    new StringSelection(Helpers.convertToLowerCaseWithCorrectPunctuation(content));
            clipboard.setContents(selection, null);

            // Simulate Delete to delete selected text
            robot.keyPress(KeyEvent.VK_DELETE);
            robot.keyRelease(KeyEvent.VK_DELETE);
            // Simulate Ctrl+V to paste new text
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            showSuccessStatusPopup();
        }
    }

    private void hidePopup() {
        float popupDelay = Float.parseFloat(appConfig.getPopUpDelay());
        if(timer != null) timer.stop();
        timer = new Timer((int)popupDelay * 1000, e -> {
            setVisible(false);
            isSuccessPopup = false;
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void maybeRectifyFalseCapLockState(NativeKeyEvent e) {
        boolean checkCapsLockOn = Character.isUpperCase(e.getKeyChar());
        if(e.getModifiers() != NativeInputEvent.SHIFT_L_MASK && checkCapsLockOn != capsLockOn) {
            capsLockOn = checkCapsLockOn;
            logger.info("Rectify caps lock state: " + checkCapsLockOn);
        } else
        if (e.getModifiers() == NativeInputEvent.SHIFT_L_MASK && checkCapsLockOn == capsLockOn) {
            capsLockOn = !checkCapsLockOn;
            logger.info("Rectify caps lock state: " + checkCapsLockOn);
        }
    }

    public static void main(String[] args) {
        invokeLater(() -> {
            try {
                UIManager.setLookAndFeel( new FlatDarkLaf() );
                CapsLockHook frame = new CapsLockHook();
                frame.setVisible(false);
            } catch (AWTException | UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
            RESET_IN_ACTION = false;
            capsLockOn = !capsLockOn;
            showCapsLockStatusPopup();
        }

        if(appConfig.getQuickFixEnabled() &&
            e.getModifiers() == NativeInputEvent.CTRL_L_MASK &&
                e.getKeyCode() == NativeKeyEvent.VC_Q) {
            quickFixUpperCaseText();
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {}

    public void nativeKeyTyped(NativeKeyEvent e) {
        maybeRectifyFalseCapLockState(e);
    }
}
