package co.uk.bittwisted.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class VersionReader {
    private final Logger logger = Logger.getLogger(VersionReader.class.getName());

    private String appName;
    private String appVersion;

    public VersionReader() {
        try (InputStream inputStream = VersionReader.class.getResourceAsStream("/version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            this.appName = properties.getProperty("app.name");
            this.appVersion = properties.getProperty("app.version");
        } catch (IOException e) {
            logger.severe("Unable to read version information: " + e.getMessage());
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}
