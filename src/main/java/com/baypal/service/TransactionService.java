package com.baypal.service;

import com.baypal.dto.TransactionView;
import com.baypal.dto.TransferRequest;
import com.baypal.exception.UserNotFoundException;
import com.baypal.model.Transaction;
import com.baypal.model.User;
import com.baypal.repository.TransactionRepository;
import com.baypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    // moves money from "sender" to whoever owns recipientEmail, and records
    // the transfer. @Transactional means if any step throws, the whole thing
    // (debit + credit + save) rolls back - nobody ends up with money vanished.
    @Transactional
    public Transaction send(User sender, TransferRequest request) {
        User receiver = userRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new UserNotFoundException("No BayPal account found for " + request.getRecipientEmail()));

        if (receiver.getId().equals(sender.getId())) {
            throw new IllegalArgumentException("You can't send money to yourself");
        }

        walletService.debit(sender, request.getAmount());
        walletService.credit(receiver, request.getAmount());

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        return transactionRepository.save(transaction);
    }

    // records a transfer for a payment request being paid - same accounting
    // as send() but reused by PaymentService so we don't duplicate the debit/credit logic
    @Transactional
    public Transaction record(User sender, User receiver, java.math.BigDecimal amount, String note) {
        walletService.debit(sender, amount);
        walletService.credit(receiver, amount);

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(amount);
        transaction.setNote(note);
        return transactionRepository.save(transaction);
    }

    // builds the "money in / money out" view used by the dashboard + history page
    public List<TransactionView> historyFor(User user) {
        return transactionRepository.findAllForUser(user).stream()
                .map(t -> toView(t, user))
                .collect(Collectors.toList());
    }

    public List<TransactionView> recentFor(User user, int limit) {
        return historyFor(user).stream().limit(limit).collect(Collectors.toList());
    }

    private TransactionView toView(Transaction t, User viewer) {
        boolean isCredit = t.getReceiver().getId().equals(viewer.getId());
        User counterparty = isCredit ? t.getSender() : t.getReceiver();
        return new TransactionView(
                t.getId(),
                isCredit ? "CREDIT" : "DEBIT",
                counterparty.getFirstName() + " " + counterparty.getLastName(),
                t.getAmount(),
                t.getNote(),
                t.getCreatedAt()
        );
    }
}
