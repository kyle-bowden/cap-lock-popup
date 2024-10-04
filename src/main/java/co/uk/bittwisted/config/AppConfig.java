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

    public static Position DEFAULT_POSITION = Position.BOTTOM_RIGHT;
    public static String DEFAULT_POPUP_DELAY = Helpers.formatWithOneDecimalPlace(2f);

    public AppConfig(String appDataFolderPath) {
        File settingsDir = new File(appDataFolderPath);
        if(!settingsDir.exists()) {
            boolean success = settingsDir.mkdir();
            if(!success) {
                logger.log(Level.SEVERE, "Failed to create properties directory!");
                System.exit(1);
            }
        }

        String configFileName = "caplockhook.properties";

        properties = new Properties();
        configFile = new File(appDataFolderPath, configFileName);

        if(!configFile.exists()) {
            properties.setProperty(PROPERTY_CLIENT_ID, UUID.randomUUID().toString());
            updateConfig(DEFAULT_POSITION, DEFAULT_POPUP_DELAY);
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

    public String getLocation() {
        return properties.getProperty(PROPERTY_LOCATION);
    }

    public String getClientId() {
        return properties.getProperty(PROPERTY_CLIENT_ID);
    }

    public String getPopUpDelay() {
        return properties.getProperty(PROPERTY_POPUP_DELAY);
    }
}
