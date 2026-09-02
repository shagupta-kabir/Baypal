package com.baypal.exception;

// thrown when we look up a user by email (e.g. sending money to someone)
// and no account exists with that address
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
