package com.packetscope.desktop;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {

    private Connection con;

    public DbConnection() {
        setConnection();
    }

    public void setConnection() {
        String url = "jdbc:mysql://localhost:3306/packetscope";
        String username = "root";
        String password = "password";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);
        } catch (Exception ex) {
            System.out.println("mysql error: " + ex.getMessage());
        }
    }

    public Connection getConnection() {        
        return con;
    }

}
