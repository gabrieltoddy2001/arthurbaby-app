package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final ProdutoVariacaoRepository variacoes;
    private final CupomRepository cupons;
    private final EstoqueService estoque;
    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios, ProdutoRepository produtos,
                         ProdutoVariacaoRepository variacoes, CupomRepository cupons, EstoqueService estoque) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.produtos = produtos;
        this.variacoes = variacoes;
        this.cupons = cupons;
        this.estoque = estoque;
    }

    /**
     * O desconto e sempre global (nivel do pedido): itens nao carregam desconto proprio.
     * subtotal = soma(valorUnitario x quantidade); total = subtotal - desconto + frete.
     * Com cupom, o desconto e calculado aqui sobre o subtotal e o valor enviado pelo cliente e ignorado;
     * sem cupom, vale o {@code desconto} informado na requisicao.
     */
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        if (request.itens() == null || request.itens().isEmpty()) throw new IllegalArgumentException("Pedido deve possuir pelo menos um item");
        Usuario cliente = usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado"));
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido("AB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        pedido.setCliente(cliente);
        if (request.formaRecebimento() != null) pedido.setFormaRecebimento(request.formaRecebimento());
        pedido.setObservacao(request.observacao());
        pedido.setFrete(nvl(request.frete()));
        if (pedido.getFrete().signum() < 0) throw new IllegalArgumentException("Frete nao pode ser negativo");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoItemRequest itemRequest : request.itens()) {
            Produto produto = produtos.findById(itemRequest.produtoId()).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            ProdutoVariacao variacao = itemRequest.variacaoId() == null ? null : variacoes.findById(itemRequest.variacaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Variacao nao encontrada"));
            int quantidade = itemRequest.quantidade() == null ? 0 : itemRequest.quantidade();
            if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
            BigDecimal valorUnitario = variacao != null && variacao.getPreco() != null ? variacao.getPreco() :
                    (produto.getPrecoPromocional() != null ? produto.getPrecoPromocional() : produto.getPreco());
            BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setVariacao(variacao);
            item.setCodigoProduto(produto.getCodigo());
            item.setNomeProduto(produto.getNome());
            item.setVariacaoDescricao(descrever(variacao));
            item.setQuantidade(quantidade);
            item.setValorUnitario(valorUnitario);
            item.setValorTotal(totalItem);
            pedido.getItens().add(item);
            subtotal = subtotal.add(totalItem);
            estoque.baixarEstoque(produto, variacao, quantidade, cliente, "Pedido " + pedido.getNumeroPedido());
        }
        pedido.setSubtotal(subtotal);
        aplicarDesconto(pedido, request, subtotal);
        pedido.setTotal(subtotal.subtract(pedido.getDesconto()).add(pedido.getFrete()));
        registrarHistorico(pedido, null, PedidoStatus.PEDIDO_GERADO, cliente, "Pedido gerado");
        return PedidoResponse.de(pedidos.save(pedido));
    }

    private void aplicarDesconto(Pedido pedido, PedidoRequest request, BigDecimal subtotal) {
        if (request.cupom() != null && !request.cupom().isBlank()) {
            Cupom cupom = cupons.findByCodigoIgnoreCase(request.cupom().trim())
                    .filter(Cupom::estaValido)
                    .orElseThrow(() -> new IllegalArgumentException("Cupom invalido ou expirado"));
            pedido.setCupom(cupom.getCodigo());
            pedido.setDesconto(subtotal.multiply(cupom.getPercentualDesconto())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            return;
        }
        BigDecimal desconto = nvl(request.desconto());
        if (desconto.signum() < 0) throw new IllegalArgumentException("Desconto nao pode ser negativo");
        if (desconto.compareTo(subtotal) > 0) throw new IllegalArgumentException("Desconto nao pode ser maior que o subtotal");
        pedido.setDesconto(desconto);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorNumero(String numeroPedido, Usuario logado) {
        Pedido pedido = pedidos.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido nao encontrado"));
        verificarAcesso(pedido, logado);
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id, Usuario logado) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido nao encontrado"));
        verificarAcesso(pedido, logado);
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId, Usuario logado) {
        if (!equipeLoja(logado) && !logado.getId().equals(clienteId)) {
            throw new AccessDeniedException("Acesso negado a pedidos de outro cliente");
        }
        return pedidos.findByClienteIdOrderByCriadoEmDesc(clienteId).stream().map(PedidoResponse::de).toList();
    }

    @Transactional
    public PedidoResponse alterarStatus(Long id, StatusPedidoRequest request) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        Usuario usuario = request.usuarioId() == null ? null : usuarios.findById(request.usuarioId()).orElse(null);
        if (request.status() == PedidoStatus.CANCELADO) {
            return cancelar(pedido, request.motivoCancelamento(), usuario, request.observacao());
        }
        PedidoStatus anterior = pedido.getStatus();
        pedido.setStatus(request.status());
        if (request.status() == PedidoStatus.CONFIRMADO) pedido.setConfirmadoEm(LocalDateTime.now());
        registrarHistorico(pedido, anterior, request.status(), usuario, request.observacao());
        return PedidoResponse.de(pedidos.save(pedido));
    }

    /** Clientes so cancelam os proprios pedidos; equipe da loja cancela qualquer um. */
    @Transactional
    public PedidoResponse cancelarPorNumero(String numeroPedido, String motivo, Usuario logado) {
        Pedido pedido = pedidos.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido nao encontrado"));
        verificarAcesso(pedido, logado);
        return cancelar(pedido, motivo, logado, null);
    }

    private PedidoResponse cancelar(Pedido pedido, String motivo, Usuario usuario, String observacao) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Cancelamento exige motivo");
        if (pedido.getStatus() == PedidoStatus.CANCELADO) throw new IllegalArgumentException("Pedido ja esta cancelado");
        if (pedido.getStatus() == PedidoStatus.ENTREGUE) throw new IllegalArgumentException("Pedido entregue nao pode ser cancelado");
        PedidoStatus anterior = pedido.getStatus();
        pedido.setStatus(PedidoStatus.CANCELADO);
        pedido.setCanceladoEm(LocalDateTime.now());
        pedido.setMotivoCancelamento(motivo);
        estoque.estornarEstoque(pedido, usuario, "Cancelamento do pedido " + pedido.getNumeroPedido());
        registrarHistorico(pedido, anterior, PedidoStatus.CANCELADO, usuario, observacao != null ? observacao : motivo);
        return PedidoResponse.de(pedidos.save(pedido));
    }

    private void verificarAcesso(Pedido pedido, Usuario logado) {
        if (!equipeLoja(logado) && !pedido.getCliente().getId().equals(logado.getId())) {
            throw new AccessDeniedException("Acesso negado a pedido de outro cliente");
        }
    }
    private boolean equipeLoja(Usuario logado) {
        return logado.getPerfil() == Perfil.ADMINISTRADOR || logado.getPerfil() == Perfil.VENDEDOR;
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
