package com.baypal.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

// @ControllerAdvice = applies to every @Controller in the app.
// instead of every controller method wrapping its business logic in a
// try/catch, we throw our custom exceptions from the service layer and
// let this class turn them into a friendly error page.
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ModelAndView handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    // covers the plain IllegalArgumentException/IllegalStateException thrown
    // by the service layer for things like "can't send money to yourself" -
    // these already carry a useful message, so we show it as-is
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ModelAndView handleBadRequest(RuntimeException ex) {
        log.info("Rejected request: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    // thrown by Spring Security when a @PreAuthorize check fails - e.g. a
    // logged-in USER somehow POSTs straight to /admin/users/{id}/add-money.
    // Without this handler it would fall into the generic Exception.class
    // catch-all below and log as if it were a bug; it isn't one - it's the
    // security check doing exactly its job, so we log it at WARN (someone
    // tried something they weren't allowed to) and return a proper 403.
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        log.warn("Blocked an unauthorized action: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "You don't have permission to do that.");
        return mav;
    }

    // catch-all so an unexpected bug shows our styled error page instead of
    // Spring's default whitelabel error page
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "Something went wrong on our end. Please try again.");
        return mav;
    }
}
