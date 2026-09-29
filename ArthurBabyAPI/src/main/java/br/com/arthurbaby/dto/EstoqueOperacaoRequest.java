package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo das operações manuais de estoque. {@code quantidade}: unidades para entrada/estorno, ou o novo estoque total no ajuste. */
@Schema(description = "Quantidade e observação da movimentação de estoque")
public record EstoqueOperacaoRequest(
        @Schema(description = "Entrada/estorno: unidades a somar. Ajuste: novo estoque total da variação", example = "20") Integer quantidade,
        @Schema(description = "Observação livre registrada na movimentação", example = "Recebimento NF 1234 - fornecedor Baby Têxtil") String observacao) {}
