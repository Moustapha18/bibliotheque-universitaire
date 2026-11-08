package com.exam.shared.error;

import com.exam.shared.error.ErrorResponse.ValidationDetail;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

/** Point unique de traduction des exceptions en réponses {@link ErrorResponse}. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Object> handleConflict(ConflictException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violation d'intégrité : {}", ex.getMostSpecificCause().getMessage());
        return response(HttpStatus.CONFLICT, "Opération refusée : contrainte d'intégrité des données violée", List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationDetail> details = ex.getConstraintViolations().stream()
                .map(v -> new ValidationDetail(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Paramètres invalides", details);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handleInvalidSort(PropertyReferenceException ex) {
        return response(HttpStatus.BAD_REQUEST, "Critère de tri invalide : " + ex.getPropertyName(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ValidationDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, "Données de la requête invalides", details);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Corps de requête illisible ou mal formé", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        return response(status, status.getReasonPhrase(), List.of());
    }

    private ResponseEntity<Object> response(HttpStatus status, String message, List<ValidationDetail> details) {
        ErrorResponse body = new ErrorResponse(status.value(), message, Instant.now(), details);
        return new ResponseEntity<>(body, status);
    }
}
