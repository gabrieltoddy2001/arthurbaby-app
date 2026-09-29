package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corpo do {@code POST /api/pedidos}. Os valores monetários são recalculados no servidor:
 * {@code desconto} e {@code frete} existem apenas por compatibilidade com o app e são ignorados.
 */
@Schema(description = "Dados para criação do pedido")
public record PedidoRequest(
        @Schema(description = "Id do cliente dono do pedido", example = "3") Long clienteId,
        @Schema(description = "Retirada na loja (frete 0) ou entrega", example = "ENTREGA") FormaRecebimento formaRecebimento,
        @Schema(description = "Código do cupom (opcional)", example = "ARTHUR10") String cupom,
        @Schema(description = "Ignorado: o desconto é calculado a partir do cupom", example = "0", deprecated = true) BigDecimal desconto,
        @Schema(description = "Ignorado: o frete é calculado no servidor", example = "0", deprecated = true) BigDecimal frete,
        @Schema(description = "Observação do cliente para a loja", example = "Entregar após as 14h") String observacao,
        @Schema(description = "Itens do pedido (ao menos um)") List<PedidoItemRequest> itens) {}
