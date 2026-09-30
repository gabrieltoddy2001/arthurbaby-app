package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Par cliente/produto usado para adicionar ou remover um favorito. */
@Schema(description = "Cliente e produto do favorito")
public record FavoritoRequest(
        @Schema(description = "Id do cliente (deve ser o próprio usuário logado, salvo admin/vendedor)", example = "3") Long clienteId,
        @Schema(description = "Id do produto", example = "1") Long produtoId) {}
