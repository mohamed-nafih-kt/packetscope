package com.packetscope.desktop.service;

import com.packetscope.desktop.model.User;
import com.packetscope.desktop.repository.LoginDao;

public class LoginService {

    public boolean validateUser(String username, String password) {
        LoginDao lr = new LoginDao();
        User user = lr.getUserDetails(username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

}
