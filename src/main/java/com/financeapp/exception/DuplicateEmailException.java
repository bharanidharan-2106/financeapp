package com.financeapp.exception;

public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEmailException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}
