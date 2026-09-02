package com.baypal.config;

import com.baypal.model.Role;
import com.baypal.model.User;
import com.baypal.model.Wallet;
import com.baypal.repository.UserRepository;
import com.baypal.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// CommandLineRunner.run() fires once, right after the app starts.
// We use it to seed an AUTHOR, ADMIN, and demo USER account so you have
// something to log in with immediately, without touching the database by hand.
// IMPORTANT: change/remove these credentials before deploying anywhere real.
//
// Each account is checked/created independently (see createIfMissing()) -
// so adding a new seed account here always takes effect on the next
// restart, even against a database that already has other users in it.
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // admin starts funded so it can actually send money to users -
        // previously seeded at $0.00, which meant every admin -> user
        // transfer failed with "not enough balance to complete this transfer"
        createIfMissing("Admin", "User", "admin@baypal.com", "admin123", Role.ADMIN, new BigDecimal("1000.00"));
        createIfMissing("Demo", "User", "demo@baypal.com", "demo123", Role.USER, new BigDecimal("500.00"));
        // author doesn't send/receive peer-to-peer money, so $0 is fine -
        // its wallet only exists because every User row needs one (see
        // User.wallet / Wallet.user, both non-nullable)
        createIfMissing("Author", "Owner", "author@baypal.com", "author123", Role.AUTHOR, BigDecimal.ZERO);
    }

    private void createIfMissing(String firstName, String lastName, String email, String rawPassword,
                                 Role role, BigDecimal startingBalance) {
        if (userRepository.existsByEmail(email)) {
            // already seeded on a previous run - don't create a duplicate
            return;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        User saved = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(startingBalance);
        walletRepository.save(wallet);

        log.info("Seeded {} account -> {} / {}", role, email, rawPassword);
    }
}