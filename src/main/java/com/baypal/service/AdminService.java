package com.baypal.service;

import com.baypal.model.Role;
import com.baypal.model.Transaction;
import com.baypal.model.User;
import com.baypal.repository.TransactionRepository;
import com.baypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

// everything the /admin/** pages need - listing every user, every
// transaction, crediting a user's wallet, and toggling an account on/off.
// kept separate from UserService/TransactionService since a normal user
// should never be able to call these.
//
// IMPORTANT: an admin only ever manages plain USER accounts here - never
// other ADMIN accounts and never AUTHOR accounts. creditWallet() and
// toggleEnabled() both check this below. Managing ADMIN accounts is the
// AUTHOR role's job (see AuthorService) - an admin being able to freeze a
// fellow admin, or worse an author, would let a compromised admin account
// escalate its way up the hierarchy.
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    public List<User> allUsers() {
        return userRepository.findByRole(Role.USER);
    }

    public List<Transaction> allTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    // the ONLY place in the whole app that adds money to a wallet out of
    // thin air. Everywhere else, a balance only moves because money came
    // FROM another wallet (TransactionService.send/record). This method
    // exists to model an admin depositing funds after verifying something
    // outside the app - e.g. the user wired cash to the company's bank
    // account and support is crediting their BayPal balance to match.
    // AdminController.creditWallet() is what calls this, and that method is
    // both URL-locked (/admin/**) and @PreAuthorize-locked - a regular user
    // has no route in the app that reaches this method.
    public void creditWallet(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.USER) {
            throw new IllegalArgumentException("Only user accounts can be credited from the admin panel");
        }
        walletService.credit(user, amount);
    }

    // freezes/unfreezes an account - a disabled user can't log in
    // (see CustomUserDetailsService, which reads this flag)
    public void toggleEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.USER) {
            throw new IllegalArgumentException("Only user accounts can be frozen from the admin panel");
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    public long totalUserCount() {
        return userRepository.countByRole(Role.USER);
    }

    public long totalTransactionCount() {
        return transactionRepository.count();
    }
}

