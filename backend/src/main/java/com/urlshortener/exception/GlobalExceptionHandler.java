package com.urlshortener.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        "Invalid username or password",
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    ValidationErrorResponse errorResponse = new ValidationErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        "Validation failed",
        errors,
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.BAD_REQUEST.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        ex.getMessage(),
        request.getDescription(false),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static class ErrorResponse {
    private final int status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {
      this.status = status;
      this.message = message;
      this.path = path;
      this.timestamp = timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

    public String getPath() {
      return path;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }
  }

  public static class ValidationErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> errors;
    private final LocalDateTime timestamp;

    public ValidationErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
      this.status = status;
      this.message = message;
      this.errors = errors;
      this.timestamp = timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

    public Map<String, String> getErrors() {
      return errors;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }
  }
}
