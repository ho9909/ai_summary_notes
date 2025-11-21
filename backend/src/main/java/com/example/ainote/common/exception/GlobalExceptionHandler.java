package com.example.ainote.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<String> msgs = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> fe.getDefaultMessage()).toList();

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(req.getRequestURI(), "Bad Request", "VALIDATION_FAILED", msgs));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegal(
            IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(req.getRequestURI(), "Bad Request", "INVALID_ARGUMENT", List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(req.getRequestURI(), "Internal Server Error", "INTERNAL_ERROR",
                        List.of("unexpected error")));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex, HttpServletRequest req) {
        
        String message = "Header '" + ex.getHeaderName() + "' is required";
        
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(req.getRequestURI(), "Bad Request", "MISSING_HEADER", List.of(message)));
    }
}
