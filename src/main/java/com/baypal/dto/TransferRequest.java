package com.baypal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// carries the "send money" form fields (transaction/send-money.html)
@Data
public class TransferRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Enter a valid email")
    private String recipientEmail;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private java.math.BigDecimal amount;

    private String note;
}
