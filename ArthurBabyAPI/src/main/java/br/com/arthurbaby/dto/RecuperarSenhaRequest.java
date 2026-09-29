package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo do {@code POST /api/auth/recuperar-senha}. */
@Schema(description = "E-mail da conta a recuperar")
public record RecuperarSenhaRequest(
        @Schema(description = "E-mail cadastrado", example = "ana.souza@email.com") String email) {}
