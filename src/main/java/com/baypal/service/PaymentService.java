package com.baypal.service;

import com.baypal.dto.PaymentRequestDto;
import com.baypal.exception.UserNotFoundException;
import com.baypal.model.Payment;
import com.baypal.model.User;
import com.baypal.repository.PaymentRepository;
import com.baypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    // "requester" wants money FROM "payer" - this just creates the request,
    // no money moves until the payer chooses to pay it
    public Payment createRequest(User requester, PaymentRequestDto dto) {
        User payer = userRepository.findByEmail(dto.getPayerEmail())
                .orElseThrow(() -> new UserNotFoundException("No BayPal account found for " + dto.getPayerEmail()));

        if (payer.getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You can't request money from yourself");
        }

        Payment payment = new Payment();
        payment.setRequester(requester);
        payment.setPayer(payer);
        payment.setAmount(dto.getAmount());
        payment.setNote(dto.getNote());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    // requests where I'm the one being asked to pay
    public List<Payment> incomingFor(User payer) {
        return paymentRepository.findByPayerAndStatusOrderByCreatedAtDesc(payer, Payment.PaymentStatus.PENDING);
    }

    // requests I sent out to other people
    public List<Payment> outgoingFor(User requester) {
        return paymentRepository.findByRequesterOrderByCreatedAtDesc(requester);
    }

    // actually moves the money and marks the request as settled
    @Transactional
    public void pay(Long paymentId, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        if (!payment.getPayer().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("This request isn't addressed to you");
        }

        transactionService.record(payment.getPayer(), payment.getRequester(), payment.getAmount(), payment.getNote());

        payment.setStatus(Payment.PaymentStatus.PAID);
        paymentRepository.save(payment);
    }

    public void decline(Long paymentId, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        if (!payment.getPayer().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("This request isn't addressed to you");
        }

        payment.setStatus(Payment.PaymentStatus.DECLINED);
        paymentRepository.save(payment);
    }
}
