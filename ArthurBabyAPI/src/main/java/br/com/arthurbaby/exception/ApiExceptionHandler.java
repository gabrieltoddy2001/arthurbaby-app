package br.com.arthurbaby.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return corpo(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Dados invalidos");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> corpoInvalido(HttpMessageNotReadableException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido ou mal formatado");
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, Object>> notFound(NoSuchElementException ex) {
        return corpo(HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Registro nao encontrado");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException ex) {
        return corpo(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> erroInterno(Exception ex) {
        log.error("Erro nao tratado", ex);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("erro", mensagem);
        body.put("codigo", status.value());
        return ResponseEntity.status(status).body(body);
    }
}
