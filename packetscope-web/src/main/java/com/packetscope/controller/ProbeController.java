package com.packetscope.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProbeController {
    @GetMapping({"/probe"})
    public String goHome() {return "probe";}
}