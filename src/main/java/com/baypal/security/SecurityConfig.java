package com.baypal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// this class is the gatekeeper for the whole app - which URLs are public,
// which need any login, and which need the ADMIN or AUTHOR role specifically.
@Configuration
@EnableWebSecurity
// turns on @PreAuthorize on individual controller/service methods (see
// AdminController.creditWallet()). We already lock the whole /admin/** URL
// path to ROLE_ADMIN below, so this is a second, belt-and-suspenders check
// directly on the sensitive method itself - if that URL rule was ever
// loosened or the method got called from somewhere else, @PreAuthorize
// still stops a non-admin from crediting a wallet.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // landing page, login, register, and all static assets (css/js/images) are public
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()

                // only an ADMIN account can reach anything under /admin
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // only an AUTHOR account can reach anything under /author -
                // AUTHOR sits above ADMIN in the hierarchy (see Role.java)
                .requestMatchers("/author/**").hasRole("AUTHOR")

                // everything else (dashboard, wallet, transactions, payments) needs any logged-in user
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // our own styled page at templates/auth/login.html
                .loginPage("/login")
                // auth/login.html posts to /login with fields named "email" and "password"
                .usernameParameter("email")
                .passwordParameter("password")
                // send authors to the author dashboard, admins to the admin
                // dashboard, everyone else to their own dashboard
                .successHandler((request, response, authentication) -> {
                    boolean isAuthor = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    String redirectUrl = isAuthor ? "/author/dashboard" : isAdmin ? "/admin/dashboard" : "/dashboard";
                    response.sendRedirect(redirectUrl);
                })
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    // BCrypt is the standard, well-tested way to hash passwords before storing them
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // wires our CustomUserDetailsService + the password encoder together so
    // Spring Security knows how to actually check a login attempt
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
