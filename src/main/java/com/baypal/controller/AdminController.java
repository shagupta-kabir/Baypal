package com.baypal.controller;

import com.baypal.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

// everything under here is already locked to ROLE_ADMIN by SecurityConfig's
// URL rule (.requestMatchers("/admin/**").hasRole("ADMIN")), so we don't
// need to re-check the role in every method. creditWallet() below adds an
// extra @PreAuthorize anyway - see the comment on that method for why.
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.totalUserCount());
        model.addAttribute("transactionCount", adminService.totalTransactionCount());
        model.addAttribute("recentTransactions", adminService.allTransactions().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.allUsers());
        return "admin/users";
    }

    // IMPORTANT: crediting a wallet (adding money that didn't come from
    // another user's balance) is an admin-only action - see the long
    // comment on AdminService.creditWallet() for the reasoning. A regular
    // user is never given a link or form that posts here; the ADMIN-only
    // check below is what actually stops them if they tried to hit this
    // URL directly anyway.
    //
    // @PreAuthorize runs BEFORE the method body executes and throws
    // AccessDeniedException (-> a 403) if the currently logged-in user
    // doesn't have ROLE_ADMIN. It's redundant with the URL-level rule in
    // SecurityConfig right now, but it means this specific action stays
    // admin-only even if that URL rule is ever changed by accident later.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/add-money")
    public String creditWallet(@PathVariable Long id, @RequestParam BigDecimal amount) {
        adminService.creditWallet(id, amount);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id) {
        adminService.toggleEnabled(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions", adminService.allTransactions());
        return "admin/transactions";
    }
}

