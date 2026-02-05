package com.packetscope.db;

import com.packetscope.util.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;

public class Db {
    String url =  ConfigLoader.getDbUrl();
    String user = ConfigLoader.getDbUsername();
    String pass = ConfigLoader.getDbPassword();

    public Connection get(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url,user,pass);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }
}
