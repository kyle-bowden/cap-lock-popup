import enums.Position;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import service.AnalyticService;

import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class CapsLockHook extends JFrame implements NativeKeyListener {
    private Timer timer;
    private final Robot robot;
    private final Font defaultFont = new Font("Arial Black", Font.PLAIN, 50);
    private final Logger logger = Logger.getLogger(CapsLockHook.class.getName());
    private final SettingsView settingsView;
    private boolean capsLockState;
    private boolean isSuccessPopup;
    private boolean RESET_IN_ACTION = false;

    private final Point topLeft;
    private final Point topRight;
    private final Point topCenter;
    private final Point bottomLeft;
    private final Point bottomRight;
    private final Point bottomCenter;

    private File propertiesFile;
    public Properties properties;

    private final GradientPaint defaultBackgroundGradient;
    private final AnalyticService analyticService;
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_POPUP_DELAY = "popupDelay";

    public CapsLockHook() throws AWTException {
        setTitle("Cap Lock Hook");
        setType(Type.UTILITY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(100, 100);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

        boolean init = loadSettings();
        robot = new Robot();
        analyticService = new AnalyticService(properties.getProperty(PROPERTY_CLIENT_ID));
        defaultBackgroundGradient = new GradientPaint(100, 0, Color.BLACK, getWidth()+50, getHeight(), Color.GRAY);

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
        capsLockState = kit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                flipCapLockState();
            }
        });
        setupSystemTray();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopCapsLockHook));

        settingsView = new SettingsView(this);
        if(init)  {
            settingsView.showSettings();
        }

        updatePosition(Position.valueOf(properties.getProperty(PROPERTY_LOCATION)));
    }

    private boolean loadSettings() {
        String appDataFolder = System.getenv("APPDATA") + "\\cap-lock-hook";
        File settingsDIr = new File(appDataFolder);
        if(!settingsDIr.exists()) {
            boolean success = settingsDIr.mkdir();
            if(!success) logger.log(Level.SEVERE, "Failed to create properties directory!");
        }

        properties = new Properties();
        propertiesFile = new File(appDataFolder, "caplockhook.properties");
        if(!propertiesFile.exists()) {
            properties.setProperty(PROPERTY_CLIENT_ID, UUID.randomUUID().toString());
            updateSettings(Position.BOTTOM_RIGHT, Util.formatWithOneDecimalPlace(2f));
            return true;
        } else {
            try (FileInputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
                migrateNewProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void migrateNewProperties() {
        if(properties.getProperty(PROPERTY_CLIENT_ID) == null) {
            properties.setProperty(PROPERTY_CLIENT_ID, UUID.randomUUID().toString());
        }
    }

    private void updateSettings(Position location, String popupDelay) {
        properties.setProperty(PROPERTY_LOCATION, location.name());
        properties.setProperty(PROPERTY_POPUP_DELAY, popupDelay);

        try (FileOutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, "Cap Lock Hook Properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            logger.log(Level.SEVERE, "System tray is not supported!");
        } else {
            SystemTray tray = SystemTray.getSystemTray();

            ImageIcon icon = new ImageIcon(Objects.requireNonNull(CapsLockHook.class.getResource("icon_tray.png")));
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

            TrayIcon trayIcon = new TrayIcon(image, "Cap Lock Hook", popupMenu);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                logger.log(Level.SEVERE, "TrayIcon could not be added.");
                return;
            }

            addWindowStateListener(new java.awt.event.WindowAdapter() {
                public void windowStateChanged(java.awt.event.WindowEvent evt) {
                    if (evt.getNewState() == JFrame.ICONIFIED) {
                        try {
                            setVisible(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        String message = capsLockState ? "A" : "a";

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

        g2d.setColor(Color.BLACK);
        g2d.setPaint(defaultBackgroundGradient);
        g2d.fillRect(0, 0, 100, 100);

        if(!isSuccessPopup) {
            if(RESET_IN_ACTION) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.WHITE);
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

        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 30, 30));
        g2d.draw(new RoundRectangle2D.Double(5, 5, getWidth() - 10, getHeight() - 10, 27, 27));
    }

    private void flipCapLockState() {
        if(RESET_IN_ACTION) return;
        capsLockState = !capsLockState;

        RESET_IN_ACTION = true;
        Timer timer = new Timer(500, t -> {
            RESET_IN_ACTION = false;
        });
        timer.setRepeats(false);
        timer.start();

        repaint();

        analyticService.trackCapLockFlipToggle();
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

    public void updatePosition(Position position) {
        logger.log(Level.INFO, "POSITION:" + position);
        switch (position) {
            case TOP_LEFT -> setLocation(topLeft);
            case TOP_RIGHT -> setLocation(topRight);
            case TOP_CENTER -> setLocation(topCenter);
            case BOTTOM_LEFT -> setLocation(bottomLeft);
            case BOTTOM_RIGHT -> setLocation(bottomRight);
            case BOTTOM_CENTER -> setLocation(bottomCenter);
        }
        updateSettings(position, properties.getProperty(PROPERTY_POPUP_DELAY));
        showCapsLockStatusPopup();
    }

    public void updatePopupDelay(boolean isUp) {
        float popupDelay = Float.parseFloat(properties.getProperty(PROPERTY_POPUP_DELAY));
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
        updateSettings(Position.valueOf(properties.getProperty(PROPERTY_LOCATION)), Util.formatWithOneDecimalPlace(popupDelay));
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

        analyticService.trackCapLockToggle();
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

        String content = Util.getClipboardContentAsString();
        logger.log(Level.INFO,"Convert content:" + content);
        if(!content.equals("")) {
            StringSelection selection = new StringSelection(Util.convertToLowerCaseWithCorrectPunctuation(content));
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

        analyticService.trackCapLockQuickFix();
    }

    private void hidePopup() {
        float popupDelay = Float.parseFloat(properties.getProperty(PROPERTY_POPUP_DELAY));
        if(timer != null) timer.stop();
        timer = new Timer((int)popupDelay * 1000, e -> {
            setVisible(false);
            isSuccessPopup = false;
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            try {
                CapsLockHook frame = new CapsLockHook();
                frame.setVisible(false);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
            RESET_IN_ACTION = false;
            capsLockState = !capsLockState;
            showCapsLockStatusPopup();
        }

        if(e.getKeyCode() == NativeKeyEvent.VC_Q && e.getModifiers() == NativeInputEvent.CTRL_L_MASK) {
            quickFixUpperCaseText();
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}
