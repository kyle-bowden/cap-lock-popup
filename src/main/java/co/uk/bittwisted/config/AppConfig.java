package co.uk.bittwisted.config;

import co.uk.bittwisted.enums.Position;
import co.uk.bittwisted.util.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppConfig {
    private final Logger logger = Logger.getLogger(AppConfig.class.getName());

    private final File configFile;
    private final Properties properties;

    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_POPUP_DELAY = "popupDelay";

    private boolean isFirstTimeUser;

    public AppConfig() {
        String appDataFolderPath = System.getenv("APPDATA") + "\\cap-lock-hook";
        File settingsDir = new File(appDataFolderPath);
        if(!settingsDir.exists()) {
            boolean success = settingsDir.mkdir();
            if(!success) {
                logger.log(Level.SEVERE, "Failed to create properties directory!");
                System.exit(1);
            }
        }

        properties = new Properties();
        String configFileName = "caplockhook.properties";
        configFile = new File(appDataFolderPath, configFileName);
        if(!configFile.exists()) {
            properties.setProperty(PROPERTY_CLIENT_ID, UUID.randomUUID().toString());
            updateConfig(Position.BOTTOM_RIGHT, Helpers.formatWithOneDecimalPlace(2f));
            isFirstTimeUser = true;
        } else {
            try (FileInputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                keepClientIDAcrossNewConfigVersions();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to migrate new properties.", e);
            }
        }

    }

    public void updateConfig(Position location, String popupDelay) {
        properties.setProperty(PROPERTY_LOCATION, location.name());
        properties.setProperty(PROPERTY_POPUP_DELAY, popupDelay);

        try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
            properties.store(outputStream, "Cap Lock Hook Properties");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to store property.", e);
        }
    }

    private void keepClientIDAcrossNewConfigVersions() {
        if(properties.getProperty(PROPERTY_CLIENT_ID) == null) {
            properties.setProperty(PROPERTY_CLIENT_ID, UUID.randomUUID().toString());
        }
    }

    public boolean isFirstTimeUser() {
        return isFirstTimeUser;
    }

    public String getProperty(String key) {
        if(Optional.ofNullable(properties).isPresent()) {
            return properties.getProperty(key);
        } else {
            logger.log(Level.SEVERE, "Property could not be fetched.", key);
            System.exit(1);
        }
        return null;
    }
}
