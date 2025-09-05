package com.mdnafih.gym.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.stream.IntStream;

@Controller
public class LoginController {

    @GetMapping({"/" ,"/login"})
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        if ("admin".equals(username) && "1234".equals(password)) {
            session.setAttribute("username", username);
            model.addAttribute("message", "Login successful!");
            model.addAttribute("user", username);
            return "welcome";
        } else if ("list".equals(username) && "pass".equals(password)){
            session.setAttribute("username", username);
            ArrayList<String> garbageList = new ArrayList<>();
            IntStream.range(0,5).forEach(i -> garbageList.add("garbage" + i));
            model.addAttribute("message", "You have successfully logged in!");
            model.addAttribute("user", username);
            model.addAttribute("gbList",garbageList);
            return "welcome";
        } else {
            model.addAttribute("error", "Invalid credentials!");
            return "login";
        }
    }
}