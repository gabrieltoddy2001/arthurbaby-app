package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo do POST de avaliação. {@code clienteId} é opcional: se vier, precisa ser o próprio dono do token
 * (o cliente não avalia em nome de outro); se não vier, usa o id do token.
 */
@Schema(description = "Nova avaliação de produto")
public record AvaliacaoRequest(
        @Schema(description = "Id do cliente (opcional; deve ser o do token)", nullable = true, example = "8") Long clienteId,
        @Schema(description = "Nota de 1 a 5 estrelas", example = "5") Integer nota,
        @Schema(description = "Comentário livre", nullable = true, example = "Tecido muito macio, amei!") String comentario) {}
