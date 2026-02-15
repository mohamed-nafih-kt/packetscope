package com.packetscope.db;

import com.packetscope.util.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connectivity for the Web Application.
 */
public class Db {

    private static final Logger LOGGER = Logger.getLogger(Db.class.getName());

    private final String url;
    private final String user;
    private final String pass;

    public Db() {
        this.url = ConfigLoader.getDbUrl();
        this.user = ConfigLoader.getDbUsername();
        this.pass = ConfigLoader.getDbPassword();
    }

    /**
     * Establishes a new connection to the database.
     * return A valid Connection object.
     * throws SQLException If connectivity fails, allowing the caller to handle the error.
     */
    public Connection get() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish database connection to: " + url, e);
            throw e;
        }
    }
}
