package com.baypal.controller;

import com.baypal.dto.RegisterRequest;
import com.baypal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// handles the pages anyone can reach without being logged in:
// the landing page, the login screen, and signing up for a new wallet.
// NOTE: actually checking the login form and creating the session is done
// by Spring Security itself (see SecurityConfig) - this controller only
// needs to render the login page, not process it.
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest, Model model) {
        // registerRequest.password is intentionally cleared before we send the
        // form back to the browser on a validation failure - no reason to echo
        // a password back into the page source
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords don't match");
            model.addAttribute("registerRequest", registerRequest);
            return "auth/register";
        }

        try {
            userService.register(registerRequest);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "auth/register";
        }

        // send the new user straight to login with their account ready to go
        return "redirect:/login?registered";
    }
}
