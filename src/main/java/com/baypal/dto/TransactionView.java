package com.baypal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// a Transaction entity doesn't know if it was "money in" or "money out" -
// that's relative to whoever's dashboard is looking at it. TransactionService
// builds one of these per row so the templates can just read tx.type / tx.counterpartyName.
@Data
@AllArgsConstructor
public class TransactionView {
    private Long id;
    private String type;             // "CREDIT" (money in) or "DEBIT" (money out)
    private String counterpartyName; // the other person in the transfer
    private BigDecimal amount;
    private String note;
    private LocalDateTime createdAt;
}
