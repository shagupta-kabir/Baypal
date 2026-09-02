package com.baypal.repository;

import com.baypal.model.Payment;
import com.baypal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // requests this user needs to pay (someone requested money FROM them)
    List<Payment> findByPayerAndStatusOrderByCreatedAtDesc(User payer, Payment.PaymentStatus status);

    // requests this user sent out (asking someone else to pay THEM)
    List<Payment> findByRequesterOrderByCreatedAtDesc(User requester);
}
