package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo do {@code POST /api/auth/redefinir-senha}: token recebido por e-mail e a nova senha. */
@Schema(description = "Token de recuperação e nova senha")
public record RedefinirSenhaRequest(
        @Schema(description = "Token recebido por e-mail (em desenvolvimento, impresso no log)", example = "8f14e45f-ceea-467a-9575-3b0a4c9e2d11") String token,
        @Schema(description = "Nova senha (mínimo 4 caracteres)", example = "novaSenha123") String novaSenha) {}
