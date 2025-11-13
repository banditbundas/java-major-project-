package com.banking.controller;

import com.banking.model.User;
import com.banking.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final AuthService authService;

    public WebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Let the page load and JavaScript will handle authentication via JWT token
        // The page will redirect to login if no token is found
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        // Let the page load and JavaScript will handle authentication via JWT token
        return "accounts";
    }

    @GetMapping("/transfer")
    public String transfer(Model model) {
        // Let the page load and JavaScript will handle authentication via JWT token
        return "transfer";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        // Let the page load and JavaScript will handle authentication via JWT token
        return "transactions";
    }
}

