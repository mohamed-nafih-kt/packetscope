package com.packetscope.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application-dev.properties")) {
            if (input == null) {
                System.out.println("[ERROR] application-dev.properties not found on classpath");
            } else {
                properties.load(input);
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] Failed to load configuration: " + ex.getMessage());
        }
    }

    public static String getDbUrl() { return properties.getProperty("db.url"); }
    public static String getDbUsername() { return properties.getProperty("db.username"); }
    public static String getDbPassword() { return properties.getProperty("db.password"); }

}