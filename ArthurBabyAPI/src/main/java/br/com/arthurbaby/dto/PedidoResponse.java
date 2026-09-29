package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Pedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Resposta de pedido no formato descrito em CONTRATOS_API.md (numero, data, cliente resumido, itens, historico). */
@Schema(description = "Pedido com itens, valores e histórico de status")
public record PedidoResponse(
        @Schema(description = "Id do pedido", example = "15") Long id,
        @Schema(description = "Número do pedido (AB + data/hora)", example = "AB20260928143512345") String numero,
        @Schema(description = "Status atual", example = "PEDIDO_GERADO") String status,
        @Schema(description = "Data/hora de criação", example = "2026-09-28T14:35:12") LocalDateTime data,
        @Schema(description = "Cliente dono do pedido") ClienteResumo cliente,
        @Schema(description = "Itens do pedido") List<ItemResponse> itens,
        @Schema(description = "Soma dos itens", example = "259.80") BigDecimal subtotal,
        @Schema(description = "Cupom aplicado", example = "ARTHUR10") String cupom,
        @Schema(description = "Desconto do cupom", example = "25.98") BigDecimal desconto,
        @Schema(description = "Frete (0 na retirada ou acima de R$ 200,00)", example = "0.00") BigDecimal frete,
        @Schema(description = "Subtotal - desconto + frete", example = "233.82") BigDecimal total,
        @Schema(description = "Forma de recebimento", example = "ENTREGA", allowableValues = {"RETIRADA_LOJA", "ENTREGA"}) String formaRecebimento,
        @Schema(description = "Observação do cliente", example = "Entregar após as 14h") String observacao,
        @Schema(description = "Motivo, quando cancelado", nullable = true) String motivoCancelamento,
        @Schema(description = "Quando foi confirmado", nullable = true) LocalDateTime confirmadoEm,
        @Schema(description = "Quando foi cancelado", nullable = true) LocalDateTime canceladoEm,
        @Schema(description = "Histórico de mudanças de status") List<HistoricoResponse> historico) {

    /** Identificação mínima do cliente (sem dados pessoais sensíveis). */
    @Schema(description = "Cliente resumido")
    public record ClienteResumo(
            @Schema(description = "Id do cliente", example = "3") Long id,
            @Schema(description = "Nome do cliente", example = "Ana Souza") String nome) {}

    /** Item gravado no pedido: nome, código e preço são copiados no momento da compra. */
    @Schema(description = "Item do pedido")
    public record ItemResponse(
            @Schema(description = "Id do produto", example = "1") Long produtoId,
            @Schema(description = "Id da variação", example = "1") Long variacaoId,
            @Schema(description = "Código do produto na compra", example = "AB-001") String codigoProduto,
            @Schema(description = "Nome do produto na compra", example = "Kit Enxoval Bebe") String nomeProduto,
            @Schema(description = "Descrição da variação", example = "RN / Branco / Padrão") String variacaoDescricao,
            @Schema(description = "Quantidade", example = "2") int quantidade,
            @Schema(description = "Preço unitário na compra", example = "129.90") BigDecimal valorUnitario,
            @Schema(description = "Preço unitário x quantidade", example = "259.80") BigDecimal valorTotal) {}

    /** Entrada do histórico de status do pedido. */
    @Schema(description = "Mudança de status")
    public record HistoricoResponse(
            @Schema(description = "Status anterior (nulo na criação)", nullable = true) String statusAnterior,
            @Schema(description = "Novo status", example = "PEDIDO_GERADO") String statusNovo,
            @Schema(description = "Observação", example = "Pedido criado pelo app") String observacao,
            @Schema(description = "Usuário que alterou", example = "3") Long usuarioId,
            @Schema(description = "Data/hora da mudança", example = "2026-09-28T14:35:12") LocalDateTime data) {}

    /** Deve ser chamado dentro de uma transacao: le colecoes lazy do pedido. */
    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(
                p.getId(), p.getNumeroPedido(), p.getStatus().name(), p.getCriadoEm(),
                new ClienteResumo(p.getCliente().getId(), p.getCliente().getNomeCompleto()),
                p.getItens().stream().map(i -> new ItemResponse(
                        i.getProduto().getId(), i.getVariacao() != null ? i.getVariacao().getId() : null,
                        i.getCodigoProduto(), i.getNomeProduto(), i.getVariacaoDescricao(),
                        i.getQuantidade(), i.getValorUnitario(), i.getValorTotal())).toList(),
                p.getSubtotal(), p.getCupom(), p.getDesconto(), p.getFrete(), p.getTotal(),
                p.getFormaRecebimento().name(), p.getObservacao(), p.getMotivoCancelamento(),
                p.getConfirmadoEm(), p.getCanceladoEm(),
                p.getHistorico().stream().map(h -> new HistoricoResponse(
                        h.getStatusAnterior(), h.getStatusNovo(), h.getObservacao(),
                        h.getUsuario() != null ? h.getUsuario().getId() : null, h.getCriadoEm())).toList());
    }
}
