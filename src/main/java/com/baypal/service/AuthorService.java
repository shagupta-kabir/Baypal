package com.baypal.service;

import com.baypal.model.Role;
import com.baypal.model.Transaction;
import com.baypal.model.User;
import com.baypal.repository.TransactionRepository;
import com.baypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

// everything the /author/** pages need. AUTHOR sits above ADMIN in the
// hierarchy: an author can fund an admin's wallet out of thin air (the same
// way an admin funds a user's wallet), freeze/unfreeze any ADMIN or USER
// account, and see platform-wide numbers - total revenue and every
// transaction an admin has been party to.
@Service
@RequiredArgsConstructor
public class AuthorService {

    // the platform's cut of every dollar that has ever moved through a
    // transaction: sending $100 generates $5 of revenue. This is a
    // read-only reporting figure derived from transaction volume - it does
    // NOT deduct anything from a sender or receiver's wallet, so ordinary
    // transfers still move exactly the amount the sender typed in. Rest of
    // the money-movement logic (WalletService/TransactionService) is
    // untouched.
    private static final BigDecimal REVENUE_RATE = new BigDecimal("0.05");

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    // ADMIN + USER accounts only - never other AUTHOR accounts (see
    // toggleEnabled() below for why)
    public List<User> manageableAccounts() {
        return userRepository.findByRoleIn(List.of(Role.ADMIN, Role.USER));
    }

    // the only place in the app that hands an ADMIN account money out of
    // thin air, mirroring AdminService.creditWallet() for USER accounts.
    // AuthorController.creditAdmin() is what calls this, and that method is
    // both URL-locked (/author/**) and @PreAuthorize-locked.
    public void creditAdmin(Long adminId, BigDecimal amount) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only admin accounts can be credited from the author panel");
        }
        walletService.credit(admin, amount);
    }

    // freezes/unfreezes an ADMIN or USER account. AUTHOR accounts are
    // deliberately excluded - an author freezing another author could lock
    // everyone out of the top of the hierarchy with nobody left to undo it.
    public void toggleEnabled(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (target.getRole() == Role.AUTHOR) {
            throw new IllegalArgumentException("Author accounts can't be frozen from here");
        }
        target.setEnabled(!target.isEnabled());
        userRepository.save(target);
    }

    // every transaction where either side is an admin account - lets the
    // author audit what admins are doing with the funds they've been given
    public List<Transaction> adminTransactions() {
        return transactionRepository.findAllByParticipantRole(Role.ADMIN);
    }

    // 5% of all-time transaction volume - see REVENUE_RATE above
    public BigDecimal totalRevenue() {
        BigDecimal totalVolume = transactionRepository.sumAllTransactionAmounts();
        if (totalVolume == null) {
            totalVolume = BigDecimal.ZERO;
        }
        return totalVolume.multiply(REVENUE_RATE);
    }

    public long totalAdminCount() {
        return userRepository.countByRole(Role.ADMIN);
    }

    public long totalUserCount() {
        return userRepository.countByRole(Role.USER);
    }

    public long totalTransactionCount() {
        return transactionRepository.count();
    }
}
