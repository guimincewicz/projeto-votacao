package com.cooperative.voting.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class) ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest r) { return error(HttpStatus.NOT_FOUND, e.getMessage(), r); }
    @ExceptionHandler({ConflictException.class}) ResponseEntity<ApiError> conflict(RuntimeException e, HttpServletRequest r) { return error(HttpStatus.CONFLICT, e.getMessage(), r); }
    @ExceptionHandler({SessionClosedException.class, InvalidCpfException.class, AssociateNotAllowedException.class}) ResponseEntity<ApiError> unprocessable(RuntimeException e, HttpServletRequest r) { return error(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), r); }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e, HttpServletRequest r) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message, r);
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class) ResponseEntity<ApiError> unreadable(Exception e, HttpServletRequest r) { return error(HttpStatus.BAD_REQUEST, "Invalid request body", r); }
    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) { return ResponseEntity.status(status).body(new ApiError(java.time.Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI())); }
}
