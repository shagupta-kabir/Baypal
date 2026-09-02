package com.baypal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// one row = one completed transfer of money between two users.
// we don't store "type" here (credit/debit) because that depends on who is
// looking at it - TransactionService figures that out relative to the viewer.
@Data
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    private BigDecimal amount;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
