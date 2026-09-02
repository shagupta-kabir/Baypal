package com.baypal.service;

import com.baypal.exception.InsufficientBalanceException;
import com.baypal.model.User;
import com.baypal.model.Wallet;
import com.baypal.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// every operation that touches a balance (add money, withdraw, send, receive,
// pay a request) goes through here so the "can't go below zero" rule lives
// in exactly one place.
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Wallet getWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("This account has no wallet - contact support"));
    }

    public void credit(User user, BigDecimal amount) {
        Wallet wallet = getWallet(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    public void debit(User user, BigDecimal amount) {
        Wallet wallet = getWallet(user);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Not enough balance to complete this transfer");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }
}
