package com.baypal.repository;

import com.baypal.model.Role;
import com.baypal.model.Transaction;
import com.baypal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // every transaction where the given user was either side of the transfer,
    // most recent first - this is what powers both the dashboard preview and
    // the full history page
    @Query("SELECT t FROM Transaction t WHERE t.sender = :user OR t.receiver = :user " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllForUser(@Param("user") User user);

    // used by the admin panel to see every transfer in the system
    List<Transaction> findAllByOrderByCreatedAtDesc();

    // every transaction where either side has the given role, most recent
    // first - used by AuthorService to show "transactions of admin"
    @Query("SELECT t FROM Transaction t WHERE t.sender.role = :role OR t.receiver.role = :role " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByParticipantRole(@Param("role") Role role);

    // total dollar volume ever sent across every transaction in the system -
    // AuthorService turns this into "revenue" by taking 5% of it.
    // COALESCE(...,0) so a brand new system with zero transactions returns
    // 0 instead of null.
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    BigDecimal sumAllTransactionAmounts();
}
