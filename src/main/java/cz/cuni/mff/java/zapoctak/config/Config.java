package cz.cuni.mff.java.zapoctak.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private static Connection connection = null;
    private static final Logger logger = Logger.getLogger(Config.class.getName());

    static {
        try {
            connection = createConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Failed to initialize database connection", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                connection = createConnection();
            } catch (ClassNotFoundException | SQLException e) {
                logger.log(Level.SEVERE, "Failed to create database connection", e);
                throw new RuntimeException("Failed to create database connection", e);
            }
        }
        return connection;
    }

    private static Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
    }
}
