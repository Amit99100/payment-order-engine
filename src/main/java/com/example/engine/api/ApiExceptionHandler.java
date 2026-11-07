package com.example.engine.api;

import com.example.engine.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }


    @ExceptionHandler({BusinessException.class, IllegalStateException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> badRequest(RuntimeException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).findFirst().orElse("Invalid request");
        return response(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        // Do not expose SQL, table names or database details to API clients.
        log.warn("Database constraint violation on {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return response(HttpStatus.CONFLICT, "A conflicting record already exists", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected API error on {}", req.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(),
            status.getReasonPhrase(), message, req.getRequestURI()));
    }

    public record ApiError(Instant timestamp, int status, String error, String message, String path) {}
}
