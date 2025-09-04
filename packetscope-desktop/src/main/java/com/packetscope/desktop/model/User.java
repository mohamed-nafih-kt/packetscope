package com.packetscope.desktop.model;

public class User {

    private String username;
    private String password;

    public User(String name, String password) {
        this.username = name;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }   

    public void setPassword(String password) {
        this.password = password;
    }
    public void setUsername(String name) {
        this.username = name;
    }

}
