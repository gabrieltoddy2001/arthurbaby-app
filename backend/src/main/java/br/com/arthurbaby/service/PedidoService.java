package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final ProdutoVariacaoRepository variacoes;
    private final EstoqueService estoque;
    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios, ProdutoRepository produtos,
                         ProdutoVariacaoRepository variacoes, EstoqueService estoque) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.produtos = produtos;
        this.variacoes = variacoes;
        this.estoque = estoque;
    }
    @Transactional
    public Pedido criar(PedidoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) throw new IllegalArgumentException("Pedido deve possuir pelo menos um item");
        Usuario cliente = usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado"));
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido("AB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        pedido.setCliente(cliente);
        if (request.formaRecebimento() != null) pedido.setFormaRecebimento(request.formaRecebimento());
        pedido.setObservacao(request.observacao());
        pedido.setDesconto(nvl(request.desconto()));
        pedido.setFrete(nvl(request.frete()));
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoItemRequest itemRequest : request.itens()) {
            Produto produto = produtos.findById(itemRequest.produtoId()).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            ProdutoVariacao variacao = itemRequest.variacaoId() == null ? null : variacoes.findById(itemRequest.variacaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Variacao nao encontrada"));
            int quantidade = itemRequest.quantidade() == null ? 0 : itemRequest.quantidade();
            if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
            BigDecimal valorUnitario = variacao != null && variacao.getPreco() != null ? variacao.getPreco() :
                    (produto.getPrecoPromocional() != null ? produto.getPrecoPromocional() : produto.getPreco());
            BigDecimal descontoItem = nvl(itemRequest.desconto());
            BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade)).subtract(descontoItem);
            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setVariacao(variacao);
            item.setCodigoProduto(produto.getCodigo());
            item.setNomeProduto(produto.getNome());
            item.setVariacaoDescricao(descrever(variacao));
            item.setQuantidade(quantidade);
            item.setValorUnitario(valorUnitario);
            item.setDesconto(descontoItem);
            item.setValorTotal(totalItem);
            pedido.getItens().add(item);
            subtotal = subtotal.add(totalItem);
            estoque.baixarEstoque(produto, variacao, quantidade, cliente, "Pedido " + pedido.getNumeroPedido());
        }
        pedido.setSubtotal(subtotal);
        pedido.setTotal(subtotal.subtract(pedido.getDesconto()).add(pedido.getFrete()));
        registrarHistorico(pedido, null, PedidoStatus.PEDIDO_GERADO, cliente, "Pedido gerado");
        return pedidos.save(pedido);
    }
    @Transactional
    public Pedido alterarStatus(Long id, StatusPedidoRequest request) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        if (request.status() == PedidoStatus.CANCELADO && (request.motivoCancelamento() == null || request.motivoCancelamento().isBlank())) {
            throw new IllegalArgumentException("Cancelamento exige motivo");
        }
        Usuario usuario = request.usuarioId() == null ? null : usuarios.findById(request.usuarioId()).orElse(null);
        PedidoStatus anterior = pedido.getStatus();
        pedido.setStatus(request.status());
        if (request.status() == PedidoStatus.CONFIRMADO) pedido.setConfirmadoEm(LocalDateTime.now());
        if (request.status() == PedidoStatus.CANCELADO) {
            pedido.setCanceladoEm(LocalDateTime.now());
            pedido.setMotivoCancelamento(request.motivoCancelamento());
        }
        registrarHistorico(pedido, anterior, request.status(), usuario, request.observacao());
        return pedidos.save(pedido);
    }
    private void registrarHistorico(Pedido pedido, PedidoStatus anterior, PedidoStatus novo, Usuario usuario, String obs) {
        PedidoStatusHistorico historico = new PedidoStatusHistorico();
        historico.setPedido(pedido);
        historico.setUsuario(usuario);
        historico.setStatusAnterior(anterior == null ? null : anterior.name());
        historico.setStatusNovo(novo.name());
        historico.setObservacao(obs);
        pedido.getHistorico().add(historico);
    }
    private String descrever(ProdutoVariacao variacao) {
        if (variacao == null) return null;
        String tamanho = variacao.getTamanho() == null ? "" : "Tamanho: " + variacao.getTamanho().getNome();
        String cor = variacao.getCor() == null ? "" : " Cor: " + variacao.getCor().getNome();
        String modelo = variacao.getModelo() == null ? "" : " Modelo: " + variacao.getModelo().getNome();
        return (tamanho + cor + modelo).trim();
    }
    private BigDecimal nvl(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor; }
}
