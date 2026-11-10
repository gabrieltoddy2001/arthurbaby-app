package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Enums.ProdutoStatus;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Enums.VariacaoStatus;
import br.com.arthurbaby.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    private final ProdutoVariacaoRepository variacoes;
    private final CupomService cupons;
    private final FreteService frete;
    private final EstoqueService estoque;
    public PedidoService(PedidoRepository pedidos, UsuarioRepository usuarios, ProdutoRepository produtos,
                         ProdutoVariacaoRepository variacoes, CupomService cupons, FreteService frete,
                         EstoqueService estoque) {
        this.pedidos = pedidos;
        this.usuarios = usuarios;
        this.produtos = produtos;
        this.variacoes = variacoes;
        this.cupons = cupons;
        this.frete = frete;
        this.estoque = estoque;
    }

    /**
     * Todos os valores monetarios sao calculados aqui; {@code desconto} e {@code frete} enviados pelo app sao ignorados.
     * O desconto e global (nivel do pedido) e so existe via cupom cadastrado; itens nao carregam desconto proprio.
     * subtotal = soma(valorUnitario x quantidade); frete pela {@link FreteService} (zero com cupom FRETE_GRATIS);
     * total = subtotal - desconto + frete, nunca negativo.
     */
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        return PedidoResponse.de(montarPedido(request, null));
    }

    /**
     * Pedido lancado pela equipe da loja no painel (venda de balcao/WhatsApp): sempre vinculado a um
     * cliente ativo com perfil CLIENTE; o usuario logado fica registrado como vendedor do pedido.
     */
    @Transactional
    public PedidoAdminResponse criarPeloPainel(PedidoRequest request, Usuario vendedor) {
        if (request.clienteId() == null) throw new IllegalArgumentException("Selecione o cliente do pedido.");
        Usuario cliente = usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        if (cliente.getPerfil() != Perfil.CLIENTE) throw new IllegalArgumentException("O pedido deve ser vinculado a um cliente (perfil CLIENTE).");
        if (cliente.getStatus() != UsuarioStatus.ATIVO) throw new IllegalArgumentException("Cliente inativo ou bloqueado não pode receber pedidos.");
        return PedidoAdminResponse.de(montarPedido(request, vendedor));
    }

    @Transactional(readOnly = true)
    public List<PedidoAdminResponse> listarParaPainel() {
        return pedidos.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getCriadoEm, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(PedidoAdminResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoAdminResponse buscarParaPainel(Long id) {
        return PedidoAdminResponse.de(pedidos.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido não encontrado.")));
    }

    /** Alteracao de status feita no painel: o responsavel registrado no historico e sempre o usuario logado. */
    @Transactional
    public PedidoAdminResponse alterarStatusPeloPainel(Long id, StatusPedidoRequest request, Usuario logado) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
        aplicarStatus(pedido, request, logado);
        return PedidoAdminResponse.de(pedidos.save(pedido));
    }

    private Pedido montarPedido(PedidoRequest request, Usuario vendedor) {
        if (request.itens() == null || request.itens().isEmpty()) throw new IllegalArgumentException("O pedido precisa ter pelo menos um item.");
        if (request.clienteId() == null) throw new IllegalArgumentException("Informe o cliente do pedido.");
        Usuario cliente = usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido("AB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
        pedido.setCliente(cliente);
        pedido.setVendedor(vendedor);
        if (request.formaRecebimento() != null) pedido.setFormaRecebimento(request.formaRecebimento());
        pedido.setObservacao(request.observacao());
        BigDecimal subtotal = BigDecimal.ZERO;
        for (PedidoItemRequest itemRequest : request.itens()) {
            if (itemRequest.produtoId() == null) throw new IllegalArgumentException("Informe o produto de cada item do pedido.");
            Produto produto = produtos.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado. Atualize a lista de produtos e tente novamente."));
            if (produto.getStatus() == ProdutoStatus.INATIVO) throw new IllegalArgumentException(MensagensLoja.produtoIndisponivel(produto));
            ProdutoVariacao variacao = validarVariacao(produto, itemRequest.variacaoId());
            int quantidade = itemRequest.quantidade() == null ? 0 : itemRequest.quantidade();
            if (quantidade <= 0) throw new IllegalArgumentException("A quantidade de " + MensagensLoja.nomeItem(produto, variacao) + " deve ser maior que zero.");
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
            estoque.baixarEstoque(produto, variacao, quantidade, vendedor != null ? vendedor : cliente, "Pedido " + pedido.getNumeroPedido());
        }
        pedido.setSubtotal(subtotal);

        BigDecimal desconto = BigDecimal.ZERO;
        boolean freteGratis = false;
        if (request.cupom() != null && !request.cupom().isBlank()) {
            CupomService.Aplicacao aplicacao = cupons.aplicar(request.cupom(), subtotal);
            pedido.setCupom(aplicacao.cupom().getCodigo());
            desconto = aplicacao.desconto();
            freteGratis = aplicacao.freteGratis();
        }
        pedido.setDesconto(desconto);
        pedido.setFrete(freteGratis ? BigDecimal.ZERO : frete.calcular(pedido.getFormaRecebimento(), subtotal));

        BigDecimal total = subtotal.subtract(desconto).add(pedido.getFrete());
        pedido.setTotal(total.signum() < 0 ? BigDecimal.ZERO : total);
        registrarHistorico(pedido, null, PedidoStatus.PEDIDO_GERADO, vendedor != null ? vendedor : cliente,
                vendedor != null ? "Pedido lançado no painel por " + vendedor.getNomeCompleto() : "Pedido gerado");
        return pedidos.save(pedido);
    }

    /**
     * Todo item vendido precisa de uma variação (tamanho/cor/modelo), pois o estoque é controlado nela.
     * Produto sem nenhuma variação não pode ser vendido (antes saía sem baixa nem conferência de estoque).
     */
    private ProdutoVariacao validarVariacao(Produto produto, Long variacaoId) {
        if (variacaoId == null) {
            boolean temVariacao = produto.getVariacoes() != null && !produto.getVariacoes().isEmpty();
            throw new IllegalArgumentException(temVariacao ? MensagensLoja.selecioneVariacao(produto) : MensagensLoja.produtoSemVariacao(produto));
        }
        ProdutoVariacao variacao = variacoes.findById(variacaoId)
                .orElseThrow(() -> new IllegalArgumentException("A opção escolhida de " + MensagensLoja.nomeItem(produto, null) + " não foi encontrada."));
        if (variacao.getProduto() != null && produto.getId() != null && !produto.getId().equals(variacao.getProduto().getId())) {
            throw new IllegalArgumentException(MensagensLoja.variacaoDeOutroProduto(produto));
        }
        if (variacao.getStatus() == VariacaoStatus.INATIVA) throw new IllegalArgumentException(MensagensLoja.variacaoInativa(produto, variacao));
        return variacao;
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorNumero(String numeroPedido, Usuario logado) {
        Pedido pedido = pedidos.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
        verificarAcesso(pedido, logado);
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id, Usuario logado) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
        verificarAcesso(pedido, logado);
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorCliente(Long clienteId, Usuario logado) {
        if (!equipeLoja(logado) && !logado.getId().equals(clienteId)) {
            throw new AccessDeniedException("Acesso negado: estes pedidos pertencem a outro cliente.");
        }
        return pedidos.findByClienteIdOrderByCriadoEmDesc(clienteId).stream().map(PedidoResponse::de).toList();
    }

    @Transactional
    public PedidoResponse alterarStatus(Long id, StatusPedidoRequest request) {
        return alterarStatus(id, request, null);
    }

    /** {@code logado} e usado como responsavel quando o corpo nao informa {@code usuarioId}. */
    @Transactional
    public PedidoResponse alterarStatus(Long id, StatusPedidoRequest request, Usuario logado) {
        Pedido pedido = pedidos.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        Usuario usuario = request.usuarioId() == null ? logado : usuarios.findById(request.usuarioId()).orElse(logado);
        aplicarStatus(pedido, request, usuario);
        return PedidoResponse.de(pedidos.save(pedido));
    }

    private void aplicarStatus(Pedido pedido, StatusPedidoRequest request, Usuario usuario) {
        if (request.status() == null) throw new IllegalArgumentException("Informe o novo status do pedido.");
        if (request.status() == PedidoStatus.CANCELADO) {
            cancelarSemSalvar(pedido, request.motivoCancelamento(), usuario, request.observacao());
            return;
        }
        if (pedido.getStatus() == PedidoStatus.CANCELADO) throw new IllegalArgumentException("Pedido cancelado não pode mudar de status.");
        if (pedido.getStatus() == PedidoStatus.ENTREGUE) throw new IllegalArgumentException("Pedido entregue não pode mudar de status.");
        if (pedido.getStatus() == request.status()) throw new IllegalArgumentException("O pedido já está com o status " + request.status() + ".");
        PedidoStatus anterior = pedido.getStatus();
        pedido.setStatus(request.status());
        if (request.status() == PedidoStatus.CONFIRMADO && pedido.getConfirmadoEm() == null) pedido.setConfirmadoEm(LocalDateTime.now());
        String obs = request.observacao() == null || request.observacao().isBlank()
                ? "Status alterado de " + anterior + " para " + request.status() : request.observacao().trim();
        registrarHistorico(pedido, anterior, request.status(), usuario, obs);
    }

    /** Clientes so cancelam os proprios pedidos; equipe da loja cancela qualquer um. */
    @Transactional
    public PedidoResponse cancelarPorNumero(String numeroPedido, String motivo, Usuario logado) {
        Pedido pedido = pedidos.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado."));
        verificarAcesso(pedido, logado);
        return cancelar(pedido, motivo, logado, null);
    }

    private PedidoResponse cancelar(Pedido pedido, String motivo, Usuario usuario, String observacao) {
        cancelarSemSalvar(pedido, motivo, usuario, observacao);
        return PedidoResponse.de(pedidos.save(pedido));
    }

    private void cancelarSemSalvar(Pedido pedido, String motivo, Usuario usuario, String observacao) {
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        if (pedido.getStatus() == PedidoStatus.CANCELADO) throw new IllegalArgumentException("Este pedido já está cancelado.");
        if (pedido.getStatus() == PedidoStatus.ENTREGUE) throw new IllegalArgumentException("Pedido entregue não pode ser cancelado.");
        PedidoStatus anterior = pedido.getStatus();
        pedido.setStatus(PedidoStatus.CANCELADO);
        pedido.setCanceladoEm(LocalDateTime.now());
        pedido.setMotivoCancelamento(motivo);
        estoque.estornarEstoque(pedido, usuario, "Cancelamento do pedido " + pedido.getNumeroPedido());
        registrarHistorico(pedido, anterior, PedidoStatus.CANCELADO, usuario,
                observacao != null ? observacao : motivo);
    }

    private void verificarAcesso(Pedido pedido, Usuario logado) {
        if (!equipeLoja(logado) && !pedido.getCliente().getId().equals(logado.getId())) {
            throw new AccessDeniedException("Acesso negado: este pedido pertence a outro cliente.");
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
}
