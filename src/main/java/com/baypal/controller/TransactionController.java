package com.baypal.controller;

import com.baypal.dto.TransactionView;
import com.baypal.dto.TransferRequest;
import com.baypal.model.User;
import com.baypal.service.TransactionService;
import com.baypal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping("/send")
    public String sendPage(Model model) {
        model.addAttribute("transferRequest", new TransferRequest());
        return "transaction/send-money";
    }

    @PostMapping("/send")
    public String send(Authentication authentication, @ModelAttribute TransferRequest transferRequest, Model model) {
        User sender = currentUser(authentication);
        transactionService.send(sender, transferRequest);
        model.addAttribute("success", "Sent $" + transferRequest.getAmount() + " to " + transferRequest.getRecipientEmail());
        model.addAttribute("transferRequest", new TransferRequest());
        return "transaction/send-money";
    }

    @GetMapping
    public String history(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("transactions", transactionService.historyFor(user));
        return "transaction/history";
    }

    @GetMapping("/{id}")
    public String details(Authentication authentication, @PathVariable Long id, Model model) {
        User user = currentUser(authentication);
        // find the one matching row out of this user's own history -
        // keeps a user from viewing someone else's transaction by guessing an id
        Optional<TransactionView> match = transactionService.historyFor(user).stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();

        if (match.isEmpty()) {
            return "redirect:/transactions";
        }

        model.addAttribute("transaction", match.get());
        return "transaction/details";
    }

    private User currentUser(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
