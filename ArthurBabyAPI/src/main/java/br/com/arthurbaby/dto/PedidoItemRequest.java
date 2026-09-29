package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Item do carrinho enviado na criação do pedido. O preço é sempre lido do banco, nunca do cliente. */
@Schema(description = "Item do pedido")
public record PedidoItemRequest(
        @Schema(description = "Id do produto", example = "1") Long produtoId,
        @Schema(description = "Id da variação (tamanho/cor/modelo) escolhida", example = "1") Long variacaoId,
        @Schema(description = "Quantidade (maior que zero e até o estoque disponível)", example = "2") Integer quantidade) {}
