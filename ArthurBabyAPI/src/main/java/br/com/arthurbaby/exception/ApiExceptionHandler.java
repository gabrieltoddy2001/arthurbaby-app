package br.com.arthurbaby.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/** Toda resposta de erro sai como JSON { "erro": mensagem, "codigo": status }, nunca a pagina HTML padrao do Spring. */
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Parametro '" + ex.getName() + "' com formato invalido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<Map<String, Object>> parametroAusente(MissingServletRequestParameterException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Parametro obrigatorio ausente: " + ex.getParameterName());
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, Object>> notFound(NoSuchElementException ex) {
        return corpo(HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Registro nao encontrado");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> rotaInexistente(NoResourceFoundException ex) {
        return corpo(HttpStatus.NOT_FOUND, "Recurso nao encontrado: /" + ex.getResourcePath());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        return corpo(HttpStatus.METHOD_NOT_ALLOWED, "Metodo " + ex.getMethod() + " nao suportado para esta rota");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Map<String, Object>> tipoConteudo(HttpMediaTypeNotSupportedException ex) {
        return corpo(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type nao suportado; use application/json");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> conflito(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return corpo(HttpStatus.CONFLICT, "Operacao viola uma restricao do banco (registro duplicado ou em uso)");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String, Object>> naoAutenticado(AuthenticationException ex) {
        return corpo(HttpStatus.UNAUTHORIZED, "Nao autenticado: token ausente, invalido ou expirado");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException ex) {
        return corpo(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> erroInterno(Exception ex) {
        // Demais excecoes do Spring MVC que ja carregam um status HTTP (ErrorResponse) mantem esse status.
        if (ex instanceof ErrorResponse er && !er.getStatusCode().is5xxServerError()) {
            HttpStatusCode status = er.getStatusCode();
            String detalhe = er.getBody().getDetail();
            return corpo(HttpStatus.valueOf(status.value()), detalhe != null ? detalhe : "Requisicao invalida");
        }
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
