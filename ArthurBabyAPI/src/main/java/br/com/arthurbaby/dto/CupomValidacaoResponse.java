package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Resultado da validação de cupom. {@code motivo} vem preenchido quando {@code valido} e false. */
@Schema(description = "Resultado da validação do cupom")
public record CupomValidacaoResponse(
        @Schema(description = "Se o cupom pode ser aplicado", example = "true") boolean valido,
        @Schema(description = "Código do cupom", example = "ARTHUR10") String codigo,
        @Schema(description = "Tipo do desconto", example = "PERCENTUAL", allowableValues = {"PERCENTUAL", "VALOR_FIXO", "FRETE_GRATIS"}) String tipo,
        @Schema(description = "Descrição do cupom", example = "10% de desconto") String descricao,
        @Schema(description = "Valor do desconto calculado sobre o subtotal, em reais", example = "12.99") BigDecimal desconto,
        @Schema(description = "Se o cupom concede frete grátis", example = "false") boolean freteGratis,
        @Schema(description = "Motivo da recusa (nulo quando válido)", nullable = true) String motivo) {}
