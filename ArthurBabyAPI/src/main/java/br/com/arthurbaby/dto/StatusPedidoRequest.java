package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Enums.PedidoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/** Corpo do {@code PUT /api/pedidos/{id}/status}. Ao mudar para CANCELADO, {@code motivoCancelamento} é obrigatório. */
@Schema(description = "Novo status do pedido")
public record StatusPedidoRequest(
        @Schema(description = "Novo status", example = "CONFIRMADO") PedidoStatus status,
        @Schema(description = "Observação gravada no histórico", example = "Pagamento confirmado via PIX") String observacao,
        @Schema(description = "Obrigatório quando status = CANCELADO", example = "Cliente desistiu da compra") String motivoCancelamento,
        @Schema(description = "Id do usuário responsável pela alteração (registrado no histórico)", example = "2") Long usuarioId) {}
