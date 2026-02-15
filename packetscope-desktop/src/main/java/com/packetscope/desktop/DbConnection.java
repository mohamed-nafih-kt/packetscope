package com.packetscope.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbConnection {
    private static final Logger LOGGER = Logger.getLogger(DbConnection.class.getName());
    private static DbConnection instance;
    private Connection con;
    private final Properties properties = new Properties();
       
    private DbConnection() {      
        String env = System.getProperty("env", "dev"); 
        String configFileName = "config-" + env + ".properties";      
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (inputStream == null) {
                throw new RuntimeException("Missing configuration file: " + configFileName);
            }
            properties.load(inputStream);            
            initConnection();
                    
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load configuration", e);
            throw new RuntimeException("Failed to initialize database configuration", e);        
        }
        
    }
    
    public static synchronized DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    private void initConnection() {        
        try {
            String url = properties.getProperty("db.url");
            if (url == null) throw new SQLException("Database URL is missing in properties file.");

            con = DriverManager.getConnection(
                url,
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
            );
            LOGGER.info("Database connected successfully.");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Primary database connection attempt failed", ex);
            throw new RuntimeException("Database unreachable. Please check your network/settings.", ex);
        }
    }

    public synchronized Connection getConnection() throws SQLException {
            if (con == null || con.isClosed()) {
                LOGGER.warning("Database connection lost. Attempting to reconnect...");
                initConnection();
            }
            return con;
        }
}
