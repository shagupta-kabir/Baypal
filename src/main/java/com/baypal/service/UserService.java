package com.baypal.service;

import com.baypal.dto.RegisterRequest;
import com.baypal.exception.UserNotFoundException;
import com.baypal.model.Role;
import com.baypal.model.User;
import com.baypal.model.Wallet;
import com.baypal.repository.UserRepository;
import com.baypal.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    // turns a signup form into a real account: creates the User row AND its
    // empty Wallet row in one go, so nobody ever ends up walletless
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with that email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        // never store the raw password - always hash it first
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);
        User saved = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
        saved.setWallet(wallet);

        return saved;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found for " + email));
    }
}
