package com.financeapp.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 3L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }
}