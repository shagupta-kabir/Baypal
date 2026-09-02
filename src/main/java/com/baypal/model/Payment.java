package com.baypal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// a "payment request" - requester is asking payer to send them money.
// this is separate from Transaction: a request only becomes a Transaction
// once it's actually paid (see PaymentService.pay()).
@Data
@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the person who is owed money and created the request
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // the person being asked to pay
    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    private BigDecimal amount;

    private String note;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING, PAID, DECLINED
    }
}
