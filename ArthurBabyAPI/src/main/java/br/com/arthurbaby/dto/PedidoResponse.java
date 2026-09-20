package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Resposta de pedido no formato descrito em CONTRATOS_API.md (numero, data, cliente resumido, itens, historico). */
public record PedidoResponse(
        Long id, String numero, String status, LocalDateTime data, ClienteResumo cliente,
        List<ItemResponse> itens, BigDecimal subtotal, String cupom, BigDecimal desconto, BigDecimal frete, BigDecimal total,
        String formaRecebimento, String observacao, String motivoCancelamento,
        LocalDateTime confirmadoEm, LocalDateTime canceladoEm, List<HistoricoResponse> historico) {

    public record ClienteResumo(Long id, String nome) {}
    public record ItemResponse(Long produtoId, Long variacaoId, String codigoProduto, String nomeProduto, String variacaoDescricao,
                               int quantidade, BigDecimal valorUnitario, BigDecimal valorTotal) {}
    public record HistoricoResponse(String statusAnterior, String statusNovo, String observacao, Long usuarioId, LocalDateTime data) {}

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
