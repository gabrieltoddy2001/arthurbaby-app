package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Pedido;
import br.com.arthurbaby.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Pedido no formato usado pelo painel web (dashboard, pedidos, clientes e relatórios).
 * Diferente de {@link PedidoResponse} (contrato do app), traz os dados de contato do cliente,
 * o vendedor que lançou o pedido e o nome de quem alterou cada status.
 */
@Schema(description = "Pedido com dados do cliente, vendedor, itens e histórico (painel admin)")
public record PedidoAdminResponse(
        Long id,
        String numeroPedido,
        String status,
        String formaRecebimento,
        @Schema(description = "APP quando criado pelo cliente; PAINEL quando lançado por um vendedor/administrador") String origem,
        Long clienteId,
        String clienteNome,
        String clienteEmail,
        String clienteTelefone,
        String clienteCpf,
        Long vendedorId,
        String vendedorNome,
        @Schema(description = "Endereço principal do cliente (o pedido não grava um endereço próprio)", nullable = true) EnderecoResumo enderecoEntrega,
        List<Item> itens,
        BigDecimal subtotal,
        String cupom,
        BigDecimal desconto,
        BigDecimal frete,
        BigDecimal total,
        String observacao,
        String motivoCancelamento,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime confirmadoEm,
        LocalDateTime canceladoEm,
        List<Historico> historico) {

    public record EnderecoResumo(String cep, String logradouro, String numero, String complemento,
                                 String bairro, String cidade, String uf, String referencia) {}

    public record Item(Long produtoId, Long variacaoId, String codigoProduto, String produtoNome, String variacaoDescricao,
                       int quantidade, BigDecimal valorUnitario, BigDecimal desconto, BigDecimal valorTotal) {}

    public record Historico(String statusAnterior, String status, String observacao, Long usuarioId, String usuario,
                            LocalDateTime data) {}

    /** Deve ser chamado dentro de uma transação: lê coleções lazy do pedido e do cliente. */
    public static PedidoAdminResponse de(Pedido p) {
        Usuario c = p.getCliente();
        Usuario v = p.getVendedor();
        return new PedidoAdminResponse(
                p.getId(), p.getNumeroPedido(), p.getStatus().name(), p.getFormaRecebimento().name(),
                v != null ? "PAINEL" : "APP",
                c.getId(), c.getNomeCompleto(), c.getEmail(), c.getTelefone(), c.getCpf(),
                v != null ? v.getId() : null, v != null ? v.getNomeCompleto() : null,
                enderecoPrincipal(c),
                p.getItens().stream().map(i -> new Item(
                        i.getProduto().getId(), i.getVariacao() != null ? i.getVariacao().getId() : null,
                        i.getCodigoProduto(), i.getNomeProduto(), i.getVariacaoDescricao(), i.getQuantidade(),
                        i.getValorUnitario(), i.getDesconto(), i.getValorTotal())).toList(),
                p.getSubtotal(), p.getCupom(), p.getDesconto(), p.getFrete(), p.getTotal(),
                p.getObservacao(), p.getMotivoCancelamento(),
                p.getCriadoEm(), p.getAtualizadoEm(), p.getConfirmadoEm(), p.getCanceladoEm(),
                p.getHistorico().stream()
                        .sorted(Comparator.comparing(h -> h.getCriadoEm() == null ? LocalDateTime.MIN : h.getCriadoEm()))
                        .map(h -> new Historico(h.getStatusAnterior(), h.getStatusNovo(), h.getObservacao(),
                                h.getUsuario() != null ? h.getUsuario().getId() : null,
                                h.getUsuario() != null ? h.getUsuario().getNomeCompleto() : "Sistema",
                                h.getCriadoEm()))
                        .toList());
    }

    private static EnderecoResumo enderecoPrincipal(Usuario c) {
        Endereco e = c.getEnderecos().stream().filter(Endereco::isPrincipal).findFirst()
                .orElse(c.getEnderecos().isEmpty() ? null : c.getEnderecos().get(0));
        if (e == null) return null;
        return new EnderecoResumo(e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getReferencia());
    }
}
