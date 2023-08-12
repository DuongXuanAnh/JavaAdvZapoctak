package cz.cuni.mff.java.zapoctak.config;

import cz.cuni.mff.java.zapoctak.global.Notification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides configuration settings and methods for database connectivity.
 */
public class Config {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/java_winter";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    /**
     * Retrieves a database connection using the specified settings.
     *
     * @return A Connection object representing the database connection.
     * @throws RuntimeException If a database connection cannot be established.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }
}
