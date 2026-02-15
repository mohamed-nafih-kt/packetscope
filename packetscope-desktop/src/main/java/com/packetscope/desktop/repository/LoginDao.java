package com.packetscope.desktop.repository;

import com.packetscope.desktop.DbConnection;
import com.packetscope.desktop.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDao {

    public User getUserDetails(String username) {
        User user = null;       
        final String sql = "SELECT username, password FROM users WHERE username = ? LIMIT 1";

        try (
            Connection con = DbConnection.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);  

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getString("username"),
                            rs.getString("password")
                    );
                }
            }

        } catch (Exception ex) {
            System.err.println("Database Error (LoginDao):" + ex.getMessage());
        }

        return user;
    }

}
