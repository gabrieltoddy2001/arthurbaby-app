package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Corpo do {@code POST /api/cupons/validar}: cupom digitado e subtotal atual do carrinho. */
@Schema(description = "Cupom a validar e subtotal do carrinho")
public record CupomValidacaoRequest(
        @Schema(description = "Código do cupom (não diferencia maiúsculas/minúsculas)", example = "ARTHUR10") String codigo,
        @Schema(description = "Subtotal dos itens do carrinho, em reais", example = "129.90") BigDecimal subtotal) {}
