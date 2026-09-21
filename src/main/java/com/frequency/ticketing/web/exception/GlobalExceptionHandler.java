package com.frequency.ticketing.web.exception;

import com.frequency.ticketing.domain.exception.InvalidTransitionException;
import com.frequency.ticketing.domain.exception.TicketConflictException;
import com.frequency.ticketing.domain.exception.TicketNotFoundException;
import com.frequency.ticketing.web.dto.ApiError;
import com.frequency.ticketing.web.dto.ApiFieldError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps every rejection path to the single consistent {@link ApiError} shape (constitution
 * Principle I). No stack traces or internal identifiers are ever returned to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ApiFieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ApiFieldError(fe.getField(), message(fe)))
            .toList();
    return ResponseEntity.badRequest()
        .body(ApiError.ofValidation("Request validation failed", request.getRequestURI(), fieldErrors));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleUnreadableBody(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(
            ApiError.of(
                ApiError.Code.VALIDATION_FAILED,
                "Request body is malformed or contains an invalid value",
                request.getRequestURI()));
  }

  @ExceptionHandler(TicketNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      TicketNotFoundException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiError.of(ApiError.Code.TICKET_NOT_FOUND, ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler(InvalidTransitionException.class)
  public ResponseEntity<ApiError> handleInvalidTransition(
      InvalidTransitionException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiError.of(ApiError.Code.INVALID_TRANSITION, ex.getMessage(), request.getRequestURI()));
  }

  @ExceptionHandler({TicketConflictException.class, OptimisticLockingFailureException.class})
  public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ApiError.of(
                ApiError.Code.TICKET_CONFLICT,
                "Ticket was modified concurrently; reload and retry.",
                request.getRequestURI()));
  }

  private static String message(FieldError fe) {
    return fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value";
  }
}
