package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Produto favoritado pelo cliente, com o resumo do produto para exibir direto na lista. */
@Schema(description = "Produto favorito do cliente")
public record FavoritoResponse(
        @Schema(description = "Id do favorito", example = "12") Long id,
        @Schema(description = "Resumo do produto favoritado") ProdutoResumoResponse produto,
        @Schema(description = "Data/hora em que foi favoritado", example = "2026-09-28T14:35:12") LocalDateTime criadoEm) {}
