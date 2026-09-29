package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.PedidoItemRequest;
import br.com.arthurbaby.dto.PedidoRequest;
import br.com.arthurbaby.dto.PedidoResponse;
import br.com.arthurbaby.entity.Cupom;
import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Enums.TipoCupom;
import br.com.arthurbaby.entity.Pedido;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.CupomRepository;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PedidoServiceTest {
    private PedidoRepository pedidos;
    private UsuarioRepository usuarios;
    private ProdutoRepository produtos;
    private CupomRepository cupons;
    private EstoqueService estoque;
    private PedidoService service;
    private Usuario cliente;
    private Produto produto;

    @BeforeEach
    void setUp() {
        pedidos = mock(PedidoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        produtos = mock(ProdutoRepository.class);
        cupons = mock(CupomRepository.class);
        estoque = mock(EstoqueService.class);
        service = new PedidoService(pedidos, usuarios, produtos, mock(ProdutoVariacaoRepository.class),
                new CupomService(cupons), new FreteService(new BigDecimal("15.00"), new BigDecimal("200.00")), estoque);

        cliente = usuario(1L, Perfil.CLIENTE);
        produto = new Produto();
        produto.setId(9L);
        produto.setCodigo("P-9");
        produto.setNome("Body");
        produto.setPreco(new BigDecimal("10.00"));
    }

    @Test
    void criarIgnoraDescontoEFreteEnviadosERecalculaNoServidor() {
        prepararPedido();

        // Requisicao adulterada: desconto enorme e frete zerado numa entrega
        PedidoRequest request = new PedidoRequest(1L, FormaRecebimento.ENTREGA, null,
                new BigDecimal("9999.99"), BigDecimal.ZERO, "presente",
                List.of(new PedidoItemRequest(9L, null, 2)));

        PedidoResponse response = service.criar(request);

        assertEquals(new BigDecimal("20.00"), response.subtotal());
        assertEquals(BigDecimal.ZERO, response.desconto());
        assertEquals(new BigDecimal("15.00"), response.frete());
        assertEquals(new BigDecimal("35.00"), response.total());
        assertEquals("presente", response.observacao());
        assertEquals(2, response.itens().getFirst().quantidade());
        verify(estoque).baixarEstoque(produto, null, 2, cliente, "Pedido " + response.numero());
    }

    @Test
    void freteEhGratisNaRetiradaEAcimaDoValorMinimo() {
        prepararPedido();

        PedidoResponse retirada = service.criar(new PedidoRequest(1L, FormaRecebimento.RETIRADA_LOJA, null,
                null, new BigDecimal("50.00"), null, List.of(new PedidoItemRequest(9L, null, 1))));
        assertEquals(BigDecimal.ZERO, retirada.frete());
        assertEquals(new BigDecimal("10.00"), retirada.total());

        PedidoResponse entregaGrande = service.criar(new PedidoRequest(1L, FormaRecebimento.ENTREGA, null,
                null, null, null, List.of(new PedidoItemRequest(9L, null, 20))));
        assertEquals(new BigDecimal("200.00"), entregaGrande.subtotal());
        assertEquals(BigDecimal.ZERO, entregaGrande.frete());
    }

    @Test
    void criarAplicaCupomPercentualIgnorandoDescontoInformado() {
        prepararPedido();
        when(cupons.findByCodigoIgnoreCase("ARTHUR10")).thenReturn(Optional.of(cupom("ARTHUR10", TipoCupom.PERCENTUAL, "10")));

        PedidoResponse response = service.criar(new PedidoRequest(1L, FormaRecebimento.RETIRADA_LOJA, " arthur10 ",
                new BigDecimal("15.00"), BigDecimal.ZERO, null,
                List.of(new PedidoItemRequest(9L, null, 2))));

        assertEquals("ARTHUR10", response.cupom());
        assertEquals(new BigDecimal("2.00"), response.desconto());
        assertEquals(new BigDecimal("18.00"), response.total());
    }

    @Test
    void cupomDeValorFixoNaoDeixaTotalNegativoEFreteGratisZeraOFrete() {
        prepararPedido();
        when(cupons.findByCodigoIgnoreCase("BEMVINDO")).thenReturn(Optional.of(cupom("BEMVINDO", TipoCupom.VALOR_FIXO, "15")));
        when(cupons.findByCodigoIgnoreCase("FRETEGRATIS")).thenReturn(Optional.of(cupom("FRETEGRATIS", TipoCupom.FRETE_GRATIS, null)));

        PedidoResponse fixo = service.criar(new PedidoRequest(1L, FormaRecebimento.RETIRADA_LOJA, "BEMVINDO",
                null, null, null, List.of(new PedidoItemRequest(9L, null, 1))));
        assertEquals(new BigDecimal("10.00"), fixo.desconto());
        assertEquals(new BigDecimal("0.00"), fixo.total());

        PedidoResponse freteGratis = service.criar(new PedidoRequest(1L, FormaRecebimento.ENTREGA, "FRETEGRATIS",
                null, null, null, List.of(new PedidoItemRequest(9L, null, 1))));
        assertEquals(BigDecimal.ZERO, freteGratis.frete());
        assertEquals(new BigDecimal("10.00"), freteGratis.total());
    }

    @Test
    void criarRejeitaCupomInexistenteOuNaoAplicavel() {
        prepararPedido();
        when(cupons.findByCodigoIgnoreCase("HACKEADO")).thenReturn(Optional.empty());
        Cupom inativo = cupom("VELHO", TipoCupom.PERCENTUAL, "50");
        inativo.setAtivo(false);
        when(cupons.findByCodigoIgnoreCase("VELHO")).thenReturn(Optional.of(inativo));

        assertThrows(IllegalArgumentException.class, () -> service.criar(new PedidoRequest(1L, FormaRecebimento.ENTREGA,
                "HACKEADO", new BigDecimal("9999.99"), BigDecimal.ZERO, null, List.of(new PedidoItemRequest(9L, null, 1)))));
        assertThrows(IllegalArgumentException.class, () -> service.criar(new PedidoRequest(1L, FormaRecebimento.ENTREGA,
                "VELHO", null, null, null, List.of(new PedidoItemRequest(9L, null, 1)))));
    }

    @Test
    void criarRejeitaPedidoSemItens() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(new PedidoRequest(1L, null, null, null, null, null, List.of())));
        verifyNoInteractions(produtos, estoque);
    }

    private void prepararPedido() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtos.findById(9L)).thenReturn(Optional.of(produto));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void clientePodeCancelarPedidoProprioEEstornaEstoque() {
        Pedido pedido = pedidoDoCliente(cliente, PedidoStatus.CONFIRMADO);
        when(pedidos.findByNumeroPedido("AB-1")).thenReturn(Optional.of(pedido));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponse response = service.cancelarPorNumero("AB-1", "Desisti da compra", cliente);

        assertEquals(PedidoStatus.CANCELADO.name(), response.status());
        assertEquals("Desisti da compra", response.motivoCancelamento());
        assertEquals(1, response.historico().size());
        verify(estoque).estornarEstoque(pedido, cliente, "Cancelamento do pedido AB-1");
    }

    @Test
    void clienteNaoPodeCancelarPedidoAlheioOuEntregue() {
        Usuario outroCliente = usuario(2L, Perfil.CLIENTE);
        Pedido pedido = pedidoDoCliente(cliente, PedidoStatus.CONFIRMADO);
        when(pedidos.findByNumeroPedido("AB-1")).thenReturn(Optional.of(pedido));

        assertThrows(AccessDeniedException.class,
                () -> service.cancelarPorNumero("AB-1", "Motivo", outroCliente));

        when(pedidos.findByNumeroPedido("AB-ENTREGUE")).thenReturn(
                Optional.of(pedidoDoCliente(cliente, PedidoStatus.ENTREGUE)));
        assertThrows(IllegalArgumentException.class,
                () -> service.cancelarPorNumero("AB-ENTREGUE", "Motivo", cliente));
    }

    private static Usuario usuario(Long id, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto("Cliente " + id);
        usuario.setEmail("cliente" + id + "@example.com");
        usuario.setPerfil(perfil);
        return usuario;
    }

    private static Pedido pedidoDoCliente(Usuario cliente, PedidoStatus status) {
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(status == PedidoStatus.ENTREGUE ? "AB-ENTREGUE" : "AB-1");
        pedido.setCliente(cliente);
        pedido.setStatus(status);
        return pedido;
    }

    private static Cupom cupom(String codigo, TipoCupom tipo, String valor) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipo(tipo);
        cupom.setValor(valor == null ? null : new BigDecimal(valor));
        return cupom;
    }
}
