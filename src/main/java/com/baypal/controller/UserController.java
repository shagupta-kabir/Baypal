package com.baypal.controller;

import com.baypal.model.User;
import com.baypal.model.Wallet;
import com.baypal.repository.UserRepository;
import com.baypal.service.PaymentService;
import com.baypal.service.TransactionService;
import com.baypal.service.UserService;
import com.baypal.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        Wallet wallet = walletService.getWallet(user);

        model.addAttribute("user", user);
        model.addAttribute("wallet", wallet);
        model.addAttribute("recentTransactions", transactionService.recentFor(user, 5));
        model.addAttribute("pendingRequests", paymentService.incomingFor(user));
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", currentUser(authentication));
        return "user/profile";
    }

    @GetMapping("/settings")
    public String settingsPage(Authentication authentication, Model model) {
        model.addAttribute("user", currentUser(authentication));
        return "user/settings";
    }

    @PostMapping("/settings/password")
    public String changePassword(Authentication authentication,
                                  @RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  Model model) {
        User user = currentUser(authentication);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Current password is incorrect");
            return "user/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        model.addAttribute("user", user);
        model.addAttribute("success", "Password updated");
        return "user/settings";
    }

    // small helper used by every controller that needs "whoever is logged in right now" -
    // Spring Security only gives us the email (the username), so we look up the full entity
    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
