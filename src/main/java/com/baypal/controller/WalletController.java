package com.baypal.controller;

import com.baypal.model.User;
import com.baypal.model.Wallet;
import com.baypal.service.TransactionService;
import com.baypal.service.UserService;
import com.baypal.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

// NOTE ON "ADD MONEY": a regular user cannot credit their own wallet from
// here. That's intentional - see AdminController.creditWallet() instead.
// A wallet's balance should only increase because (a) someone else sent them
// money (TransactionService.send), or (b) an admin deposited funds on their
// behalf (e.g. after a real bank transfer/cash deposit was verified outside
// the app). Letting a user tap "+ $1000" on their own wallet would let
// anyone print unlimited money, so that endpoint deliberately doesn't exist
// here - only under /admin/**, which is locked to ROLE_ADMIN.
@Controller
@RequiredArgsConstructor
public class WalletController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping("/wallet")
    public String wallet(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        Wallet wallet = walletService.getWallet(user);

        model.addAttribute("wallet", wallet);
        model.addAttribute("recentTransactions", transactionService.recentFor(user, 8));
        return "wallet/wallet";
    }

    @GetMapping("/wallet/withdraw")
    public String withdrawPage(Authentication authentication, Model model) {
        model.addAttribute("wallet", walletService.getWallet(currentUser(authentication)));
        return "wallet/withdraw";
    }

    @PostMapping("/wallet/withdraw")
    public String withdraw(Authentication authentication, @RequestParam BigDecimal amount, Model model) {
        User user = currentUser(authentication);
        walletService.debit(user, amount);
        model.addAttribute("wallet", walletService.getWallet(user));
        model.addAttribute("success", "Withdrew $" + amount + " from your wallet");
        return "wallet/withdraw";
    }

    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}

