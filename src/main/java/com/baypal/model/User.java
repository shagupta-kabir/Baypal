package com.baypal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// one row per account. email doubles as the username used to log in.
@Data
// lombok: generates getters/setters/toString/equals for every field below
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    // unique so two accounts can never share a login email
    @Email(message = "Enter a valid email")
    @Column(unique = true, nullable = false)
    private String email;

    // stored as a BCrypt hash - never the raw password, see SecurityConfig
    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // lets an admin freeze an account without deleting it
    private boolean enabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // every user has exactly one wallet - created together at registration time.
    // excluded from toString/equals: Wallet points back at User, and without
    // this, Lombok's generated toString() would recurse User -> Wallet -> User forever
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Wallet wallet;
}
