package org.MigrationTool.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final Properties properties = new Properties();

    static {
        logger.info("Initializing ConfigLoader: Loading configuration properties...");

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.error("Configuration file 'database.properties' not found in resources!");
                throw new IOException("Cannot find database.properties");
            }

            properties.load(input);
            logger.info("Configuration file loaded successfully.");
        } catch (IOException e) {
            logger.error("Failed to load configuration file: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Requested configuration key '{}' is missing!", key);
        } else {
            logger.debug("Retrieved config key '{}': {}", key, value);
        }
        return value;
    }
}
