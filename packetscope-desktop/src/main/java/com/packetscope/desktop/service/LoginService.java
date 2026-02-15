package com.packetscope.desktop.service;

import com.packetscope.desktop.model.User;
import com.packetscope.desktop.repository.LoginDao;
import java.util.Objects;

public class LoginService {

    public boolean validateUser(String username, String password) {
        LoginDao dao = new LoginDao();
        User user = dao.getUserDetails(username);
        
        return user != null && Objects.equals(user.getPassword(), password);
    }
}
