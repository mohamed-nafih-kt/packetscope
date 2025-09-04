package com.packetscope.desktop.repository;

import com.packetscope.desktop.DbConnection;
import com.packetscope.desktop.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDao {

    public User getUserDetails(String username) {
        DbConnection dbObject = new DbConnection();
        User user = null;

        String sql = "SELECT name, password FROM users_list WHERE name = ? LIMIT 1";

        try (
            Connection con = dbObject.getConnection(); 
            PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);  // safer: prevents SQL injection

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                    user = new User(rs.getString("name"), rs.getString("password"));
                    }
                }

        } catch (Exception ex) {
            System.out.println("loginAuth error: " + ex.getMessage());
        }

        return user;
    }

}
