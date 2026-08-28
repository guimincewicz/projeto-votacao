package com.cooperative.voting.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
