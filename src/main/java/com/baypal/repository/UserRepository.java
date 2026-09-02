package com.baypal.repository;

import com.baypal.model.Role;
import com.baypal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // used both for login (CustomUserDetailsService) and for finding a
    // recipient/payer by the email they typed into a form
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // powers AdminService.allUsers() - the admin panel only manages plain
    // USER accounts, never other admins/authors
    List<User> findByRole(Role role);

    // powers AuthorService.manageableAccounts() - the author panel manages
    // both ADMIN and USER accounts, but never other AUTHOR accounts
    List<User> findByRoleIn(List<Role> roles);

    long countByRole(Role role);
}
