package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo do {@code POST /api/auth/login}.
 * O campo {@code login} aceita tanto o e-mail quanto o CPF do usuário.
 */
@Schema(description = "Credenciais de acesso")
public record LoginRequest(
        @Schema(description = "E-mail ou CPF do usuário", example = "ana.souza@email.com") String login,
        @Schema(description = "Senha do usuário", example = "123456") String senha) {}
