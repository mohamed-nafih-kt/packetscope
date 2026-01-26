package com.packetscope.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {
    private Connection con;
    InputStream is = getClass().getClassLoader().getResourceAsStream("config-" + System.getProperty("env") + ".properties");
    
    
    public DbConnection() {
        
        // 1. Determine environment
        String env = System.getProperty("env", "dev"); 
        String configFileName = "config-" + env + ".properties";
        
        Properties props = new Properties();
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (is == null) {
                throw new IOException("Could not find configuration file: " + configFileName);
            }
            props.load(is);
            
            setConnection(props);
                    
        } catch (IOException e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
        }
        
    }

    private void setConnection(Properties props) {
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.user"); 
        String password = props.getProperty("db.password"); 

        try {
            con = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully.");
        } catch (SQLException ex) {
            System.err.println("DB connection failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return con;
    }
}
