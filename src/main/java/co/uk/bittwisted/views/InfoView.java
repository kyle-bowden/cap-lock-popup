package co.uk.bittwisted.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InfoView extends JDialog {
    private final int WINDOW_WIDTH = 525;
    private final int WINDOW_HEIGHT = 400;

    private final Logger logger = Logger.getLogger(InfoView.class.getName());

    public final Font createdByFont = new Font("Arial", Font.BOLD, 18);
    public final Font infoFont = new Font("Arial", Font.BOLD | Font.ITALIC, 24);
    public final Font warningFont = new Font("Arial", Font.BOLD | Font.ITALIC, 20);
    public final Font titleFont = new Font("Arial", Font.BOLD, 32);

    public InfoView(SettingsView settingsView) {
        super(settingsView, "Info", true);

        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultLookAndFeelDecorated(false);
        setLocation(settingsView.getX(), settingsView.getY() + settingsView.getHeight());

        setLayout(new GridLayout(1,1));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));

        ImageIcon iconImage = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemResource("icon.png")));
        Image originalImage = iconImage.getImage();
        // Resize the image
        Image resizedImage = originalImage.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        JLabel iconLabel = new JLabel(resizedIcon);
        JLabel titleLabel = new JLabel("CapUp v1.1.0");
        titleLabel.setFont(titleFont);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(25, 0)));
        titlePanel.add(titleLabel);
        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel appInfoPanel = new JPanel();
        appInfoPanel.setLayout(new BoxLayout(appInfoPanel, BoxLayout.X_AXIS));
        appInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));

        JLabel appInfoLabel = new JLabel(
                "<html><span>This application lets you check the status of your Caps Lock " +
                        "key without having to glance at your keyboard.</span></html>",
                SwingConstants.CENTER);
        appInfoLabel.setFont(infoFont);
        appInfoPanel.add(appInfoLabel);

        panel.add(appInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel warningInfoPanel = new JPanel();
        warningInfoPanel.setLayout(new BoxLayout(warningInfoPanel, BoxLayout.X_AXIS));
        warningInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 10));

        JLabel warningInfoLabel = new JLabel(
                "<html><span style=\"color:rgb(230, 26, 11)\">The popup may show the wrong Caps Lock state if another program goes " +
                        "fullscreen (e.g., a game). Right-click the system tray icon and select " +
                        "<span style=\"color:rgb(187, 187, 187)\">'Flip Caps Lock'</span> to fix it.</span></html>",
                SwingConstants.CENTER);
        warningInfoLabel.setFont(warningFont);
        warningInfoPanel.add(warningInfoLabel);

        panel.add(warningInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel devInfoPanel = new JPanel();
        devInfoPanel.setLayout(new BoxLayout(devInfoPanel, BoxLayout.X_AXIS));

        JLabel devInfoLabel = new JLabel(
                "<html><span style=\"color:rgb(187, 187, 187)\">- Developed By <a href=\"\">Kyle Bowden</a> -</span></html>",
                SwingConstants.CENTER);
        devInfoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        devInfoLabel.setFont(createdByFont);
        devInfoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.linkedin.com/in/kyle-bowden-761b366b/"));
                } catch (URISyntaxException | IOException ex) {
                    logger.log(Level.WARNING, "Unable to open link!");
                }
            }
        });
        devInfoPanel.add(devInfoLabel);

        panel.add(devInfoPanel);
        add(panel);
    }

    public void showInfo() {
        setVisible(true);
    }
}
