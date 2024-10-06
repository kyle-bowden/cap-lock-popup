package co.uk.bittwisted.views;

import co.uk.bittwisted.CapsLockHook;
import co.uk.bittwisted.config.AppConfig;
import co.uk.bittwisted.views.components.*;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsView extends JFrame {
    private final AppConfig appConfig;
    private InfoView infoView;

    private final Logger logger = Logger.getLogger(SettingsView.class.getName());

    private final int WINDOW_WIDTH = 525;
    private final int WINDOW_HEIGHT = 288;

    public final Font defaultFont = new Font("Arial", Font.PLAIN, 18);
    public final Font infoFont = new Font("Arial", Font.ITALIC, 14);

    public SettingsView(CapsLockHook clh, AppConfig config) {
        this.appConfig = config;

        setTitle("CapUp v1.1.0");
        setResizable(false);
        setAlwaysOnTop(true);
        setType(Type.UTILITY);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultLookAndFeelDecorated(true);
        getContentPane().setBackground(Color.WHITE);

        setLayout(new GridLayout(1, 2));

        PopupPositionSelector popupPositionSelector = new PopupPositionSelector(clh, appConfig);
        add(popupPositionSelector);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JCheckBox checkBoxAutoStartup = new JCheckBox("Startup on login");
        JCheckBox checkBoxQuickFixToggle = new JCheckBox("Enable Quick Fix");
        JCheckBox checkBoxMinimiseOnStart = new JCheckBox("Minimise on start");

        checkBoxAutoStartup.addItemListener(e -> {
            try {
                String keyName = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
                String appName = "CapUp";

                // Get the location of the currently running class or JAR file
                URL location = CapsLockHook.class.getProtectionDomain().getCodeSource().getLocation();
                String appPath = "";

                if(checkBoxAutoStartup.isSelected()) {
                    appPath = Paths.get(location.toURI())
                                .toString()
                                .replaceAll("app\\\\CapUp-\\d+(\\.\\d+)*\\.jar", "CapUp.exe");
                    logger.log(Level.INFO, "Path to running executable: " + appPath, appPath);
                }

                List<String> command = List.of(
                    "reg", "add", keyName, "/v", appName, "/t", "REG_SZ", "/d", appPath, "/f"
                );

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.inheritIO(); // This will redirect the command output to the console
                Process process = processBuilder.start();

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    logger.log(Level.INFO, "Registry key updated successfully!");
                    appConfig.updateAutoStartupEnabled(checkBoxAutoStartup.isSelected());
                } else {
                    logger.log(Level.SEVERE, "Registry key update failed!", exitCode);
                }
            } catch (IOException | InterruptedException ex) {
                logger.log(Level.SEVERE, "Coule not update registry!", ex);
            } catch (URISyntaxException ex) {
                logger.log(Level.SEVERE, "Coule not retrieve runtime exe path!", ex);
            }
        });
        checkBoxQuickFixToggle.addItemListener(e ->
                appConfig.updateQuickFixEnabled(checkBoxQuickFixToggle.isSelected()));
        checkBoxMinimiseOnStart.addItemListener(e ->
                appConfig.updateMinimiseOnStart(checkBoxMinimiseOnStart.isSelected()));

        checkBoxAutoStartup.setFont(defaultFont);
        checkBoxAutoStartup.setSelected(appConfig.getAutoStartupEnabled());
        checkBoxMinimiseOnStart.setFont(defaultFont);
        checkBoxMinimiseOnStart.setSelected(appConfig.getMinimiseOnStartEnabled());
        checkBoxQuickFixToggle.setFont(defaultFont);
        checkBoxQuickFixToggle.setSelected(appConfig.getQuickFixEnabled());

        panel.add(checkBoxAutoStartup);
        panel.add(Box.createRigidArea(new Dimension(0, 0)));
        JLabel autoStartupLabel = new JLabel("<html><span style=\"color:gray\">(Auto start this application after restarts.)</span></html>");
        autoStartupLabel.setFont(infoFont);
        autoStartupLabel.setPreferredSize(new Dimension(50, 50));
        panel.add(autoStartupLabel);

        panel.add(checkBoxMinimiseOnStart);
        panel.add(Box.createRigidArea(new Dimension(0, 0)));
        JLabel minimiseOnStartupLabel = new JLabel("<html><span style=\"color:gray\">(Minimise to system tray on launch.)</span></html>");
        minimiseOnStartupLabel.setFont(infoFont);
        minimiseOnStartupLabel.setPreferredSize(new Dimension(50, 50));
        panel.add(minimiseOnStartupLabel);

        panel.add(checkBoxQuickFixToggle);
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        JLabel quickFixLabel = new JLabel("<html><span style=\"color:gray\">(Use Quick Fix with <span style=\"font-weight:500\">`Left-Control + Q`</span> to convert highlighted uppercase text to lowercase.)</span></html>\n");
        quickFixLabel.setFont(infoFont);
        quickFixLabel.setPreferredSize(new Dimension(50, 50));
        panel.add(quickFixLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton infoButton = new JButton("Show Extra Info");
        infoButton.setFont(defaultFont);
        infoButton.setPreferredSize(new Dimension(50, 50));

        infoButton.addActionListener(e -> {
            if(infoView != null) {
                infoView.showInfo();
            }
        });

        panel.add(infoButton);

        add(panel);

        boolean shouldMinimiseOnStartOnlyIfNotFirstTimeUser =
                !appConfig.getMinimiseOnStartEnabled() && !appConfig.isFirstTimeUser();
        setVisible(shouldMinimiseOnStartOnlyIfNotFirstTimeUser);
    }

    public void showSettings() {
        setVisible(true);
    }

    public void setInfoView(InfoView infoView) {
        this.infoView = infoView;
    }
}
