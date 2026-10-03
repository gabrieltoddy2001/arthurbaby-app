package br.com.arthurbaby.exception;

/**
 * Lançada quando dois usuários tentam alterar o mesmo estoque ao mesmo tempo.
 * Tratada no GlobalExceptionHandler para devolver HTTP 409 (Conflict).
 */
public class ConflitoEstoqueException extends RuntimeException {
    public ConflitoEstoqueException(String message) {
        super(message);
    }
}