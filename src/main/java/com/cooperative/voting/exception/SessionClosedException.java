package com.cooperative.voting.exception;

public class SessionClosedException extends RuntimeException {

    public SessionClosedException(String message) {
        super(message);
    }
}
