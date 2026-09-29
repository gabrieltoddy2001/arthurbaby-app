package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resposta simples contendo apenas uma mensagem informativa. */
@Schema(description = "Mensagem informativa")
public record MensagemResponse(
        @Schema(description = "Mensagem", example = "Se o e-mail estiver cadastrado, você receberá um código.") String mensagem) {}
