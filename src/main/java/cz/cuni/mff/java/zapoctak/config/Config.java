package cz.cuni.mff.java.zapoctak.config;

import cz.cuni.mff.java.zapoctak.global.Notification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class provides configuration settings and methods for database connectivity.
 */
public class Config {
    private static final String CONFIG_PATH = "config.properties";

    private static Properties properties = new Properties();
    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    static {
        try {
            properties.load(new FileInputStream(CONFIG_PATH));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Chyba při čtení konfiguračního souboru.", e);
        }
    }

    /**
     * Retrieves a database connection using the specified settings.
     *
     * @return A Connection object representing the database connection.
     * @throws RuntimeException If a database connection cannot be established.
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("DATABASE_URL");
        String user = properties.getProperty("USERNAME");
        String password = properties.getProperty("PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }
}
