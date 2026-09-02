package com.baypal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// each user owns exactly one wallet. BigDecimal (not double!) because we're
// dealing with money and can't afford floating point rounding errors.
@Data
@Entity
@Table(name = "wallets")
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // excluded from toString/equals for the same reason as User.wallet above -
    // prevents an infinite User <-> Wallet recursion
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private User user;

    private BigDecimal balance = BigDecimal.ZERO;
}
