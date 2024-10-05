package co.uk.bittwisted.views;

import co.uk.bittwisted.CapsLockHook;
import co.uk.bittwisted.config.AppConfig;
import co.uk.bittwisted.enums.Position;
import co.uk.bittwisted.views.components.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SettingsView extends JFrame {
    private final int WINDOW_WIDTH = 525;
    private final int WINDOW_HEIGHT = 288;
    private String positionInfoLabel;
    private String delayInfoLabel;

    private final AppConfig appConfig;

    public final Font defaultFont = new Font("Arial", Font.PLAIN, 18);
    public final Font createdByFont = new Font("Arial", Font.BOLD, 14);
    public final Font infoFont = new Font("Arial", Font.ITALIC, 14);
    private final CapsLockHook capsLockHook;

    public SettingsView(CapsLockHook clh, AppConfig config) {
        this.capsLockHook = clh;
        this.appConfig = config;

        setTitle("CapUp Settings v1.0.3");
        setResizable(false);
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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JCheckBox checkBoxAutoStartup = new JCheckBox("Start on login");
        JCheckBox checkBoxQuickFixToggle = new JCheckBox("Enable Quick Fix");

        checkBoxQuickFixToggle.addItemListener(e ->
                appConfig.updateQuickFixEnabled(checkBoxQuickFixToggle.isSelected()));

        checkBoxAutoStartup.setFont(defaultFont);
        checkBoxAutoStartup.setSelected(true);
        checkBoxQuickFixToggle.setFont(defaultFont);
        checkBoxQuickFixToggle.setSelected(appConfig.getDefaultQuickFixEnabled());

        panel.add(checkBoxAutoStartup);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(checkBoxQuickFixToggle);
        panel.add(Box.createRigidArea(new Dimension(20, 0)));
        JLabel quickFixLabel = new JLabel("<html>(You can use Quick Fix with the shortcut <span style=\"font-weight:500\">`Left-Control + Q`</span>. If you have highlighted text that is in uppercase, it will convert it to lowercase.)</html>");
        quickFixLabel.setFont(infoFont);
        quickFixLabel.setPreferredSize(new Dimension(50, 50));
        panel.add(quickFixLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 60)));
        JLabel appInfoLabel = new JLabel("<html><span style=\"color: gray\">Created By Kyle Bowden</span></html>");
        appInfoLabel.setFont(createdByFont);
        panel.add(appInfoLabel);
        add(panel);

        setVisible(true);
        setAlwaysOnTop(true);
    }

    public void showSettings() {
        setVisible(true);
    }
}
