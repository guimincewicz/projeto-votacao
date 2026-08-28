package com.cooperative.voting.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            NotFoundException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException exception,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return buildError(exception.getStatus(), exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::formatFieldError)
                .orElse("Requisição inválida");

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidBody(HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", request);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}
