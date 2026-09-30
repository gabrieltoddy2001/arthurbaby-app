package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Corpo do {@code PUT /api/pedidos/{numeroPedido}/cancelar}.
 * O app envia {@code motivo}; {@code motivoCancelamento} e o nome usado no endpoint generico de status. Ambos sao aceitos.
 */
@Schema(description = "Motivo do cancelamento (informe motivo OU motivoCancelamento)")
public record CancelarPedidoRequest(
        @Schema(description = "Motivo do cancelamento (nome usado pelo app)", example = "Comprei o tamanho errado") String motivo,
        @Schema(description = "Alternativa a \"motivo\"; tem prioridade se ambos forem enviados", example = "Comprei o tamanho errado") String motivoCancelamento) {

    /** Retorna o motivo efetivo: prioriza {@code motivoCancelamento} e cai para {@code motivo}. */
    public String motivoEfetivo() {
        if (motivoCancelamento != null && !motivoCancelamento.isBlank()) return motivoCancelamento.trim();
        return motivo == null ? null : motivo.trim();
    }
}
