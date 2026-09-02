package com.baypal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// carries the "request money" form fields (payment/request.html) -
// named *Dto to avoid clashing with the com.baypal.model.Payment entity
@Data
public class PaymentRequestDto {

    @NotBlank(message = "Payer email is required")
    @Email(message = "Enter a valid email")
    private String payerEmail;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private java.math.BigDecimal amount;

    private String note;
}
