package com.packetscope.desktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionProd {
    private Connection con;

    public DbConnection() {
        setConnection();
    }

    private void setConnection() {
        String url = "jdbc:mysql://localhost:3306/packetscope";
        String username = "USERNAME"; // change USERNAME
        String password = "PASSWORD"; // change PASSWORd

        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            System.out.println("DB connection failed. Set Credentials");
        }
    }

    public Connection getConnection() {
        return con;
    }
}
