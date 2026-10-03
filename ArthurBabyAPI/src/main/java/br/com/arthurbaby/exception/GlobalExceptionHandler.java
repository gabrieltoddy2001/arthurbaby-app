package br.com.arthurbaby.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflitoEstoqueException.class)
    public ResponseEntity<Map<String, String>> conflitoEstoque(ConflitoEstoqueException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // HTTP 409
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest() // HTTP 400
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(java.util.NoSuchElementException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // HTTP 404
                .body(Map.of("message", e.getMessage()));
    }
}