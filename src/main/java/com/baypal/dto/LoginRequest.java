package com.baypal.dto;

import lombok.Data;

// NOTE: the actual login form is handled entirely by Spring Security's
// built-in formLogin filter (see SecurityConfig) - it reads the "email" and
// "password" request parameters straight off auth/login.html, so this dto
// isn't used by any controller today. It's kept here so that if you ever add
// a JSON login endpoint (e.g. for a mobile app), the shape is ready to go.
@Data
public class LoginRequest {
    private String email;
    private String password;
}
