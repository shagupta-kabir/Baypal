package com.baypal.controller;

import com.baypal.service.AuthorService;
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

// everything under here is already locked to ROLE_AUTHOR by SecurityConfig's
// URL rule (.requestMatchers("/author/**").hasRole("AUTHOR")), so we don't
// need to re-check the role in every method - creditAdmin() below adds an
// extra @PreAuthorize anyway, same reasoning as AdminController.creditWallet().
@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("adminCount", authorService.totalAdminCount());
        model.addAttribute("userCount", authorService.totalUserCount());
        model.addAttribute("transactionCount", authorService.totalTransactionCount());
        model.addAttribute("totalRevenue", authorService.totalRevenue());
        model.addAttribute("recentAdminTransactions", authorService.adminTransactions().stream().limit(5).toList());
        return "author/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", authorService.manageableAccounts());
        return "author/accounts";
    }

    // funding an admin's wallet (money that didn't come from another
    // account's balance) is author-only - see AuthorService.creditAdmin()
    @PreAuthorize("hasRole('AUTHOR')")
    @PostMapping("/accounts/{id}/add-money")
    public String creditAdmin(@PathVariable Long id, @RequestParam BigDecimal amount) {
        authorService.creditAdmin(id, amount);
        return "redirect:/author/accounts";
    }

    @PostMapping("/accounts/{id}/toggle")
    public String toggleAccount(@PathVariable Long id) {
        authorService.toggleEnabled(id);
        return "redirect:/author/accounts";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions", authorService.adminTransactions());
        model.addAttribute("totalRevenue", authorService.totalRevenue());
        return "author/transactions";
    }
}
