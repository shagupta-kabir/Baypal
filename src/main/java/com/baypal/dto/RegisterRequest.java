package com.baypal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// carries the register.html form fields into AuthController.
// we never bind the raw HTML form straight onto the User entity -
// keeping a separate dto means the entity's password field, role, etc.
// can't accidentally be set by whatever the browser sends.
@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String confirmPassword;
}
