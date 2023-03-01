package service;

import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticService {
    private final Properties defaultProperties;
    private final Logger logger = Logger.getLogger(AnalyticService.class.getName());

    public AnalyticService(String clientId) {
        defaultProperties = new Properties();
        defaultProperties.setProperty("t", "event");
        defaultProperties.setProperty("tid", "UA-98191047-2");
        defaultProperties.setProperty("cid", clientId);
        defaultProperties.setProperty("ev", "1");
    }

    public void trackCapLockToggle() {
        sendEventDetails("keypress","ctrl","toggle");
    }

    public void trackCapLockFlipToggle() {
        sendEventDetails("mousepress","click","toggle");
    }

    public void trackCapLockQuickFix() {
        sendEventDetails("keypress", "ctrl+q", "shortcut");
    }

    private void sendEventDetails(String category, String action, String label) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            defaultProperties.setProperty("ec", URLEncoder.encode(category, StandardCharsets.UTF_8));
            defaultProperties.setProperty("ea", URLEncoder.encode(action, StandardCharsets.UTF_8));
            defaultProperties.setProperty("el", URLEncoder.encode(label, StandardCharsets.UTF_8));
            sendRequest();
        });
    }

    private void sendRequest() {
        // Build URL with parameters
        StringBuilder urlString = new StringBuilder("https://www.google-analytics.com/collect?v=1");
        for (String key : defaultProperties.stringPropertyNames()) {
            String value = defaultProperties.getProperty(key);
            urlString.append("&").append(key).append("=").append(value);
        }

        // Make GET request
        try {
            URL url = new URL(urlString.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "CapLockHookApp");
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if(status == HttpURLConnection.HTTP_OK) {
                logger.log(Level.INFO, "event tracked successfully");
            } else {
                logger.log(Level.WARNING, "event was not tracked successfully");
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "event was not tracked successfully");
        }
    }
}
