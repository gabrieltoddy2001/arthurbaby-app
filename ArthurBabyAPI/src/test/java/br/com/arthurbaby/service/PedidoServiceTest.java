package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.PedidoItemRequest;
import br.com.arthurbaby.dto.PedidoRequest;
import br.com.arthurbaby.dto.PedidoResponse;
import br.com.arthurbaby.entity.Cupom;
import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
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
        service = new PedidoService(pedidos, usuarios, produtos, mock(ProdutoVariacaoRepository.class), cupons, estoque);

        cliente = usuario(1L, Perfil.CLIENTE);
        produto = new Produto();
        produto.setId(9L);
        produto.setCodigo("P-9");
        produto.setNome("Body");
        produto.setPreco(new BigDecimal("10.00"));
    }

    @Test
    void criarCalculaTotalComDescontoFreteEBaixaEstoque() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtos.findById(9L)).thenReturn(Optional.of(produto));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoRequest request = new PedidoRequest(1L, FormaRecebimento.ENTREGA, null,
                new BigDecimal("1.50"), new BigDecimal("2.00"), "presente",
                List.of(new PedidoItemRequest(9L, null, 2)));

        PedidoResponse response = service.criar(request);

        assertEquals(new BigDecimal("20.00"), response.subtotal());
        assertEquals(new BigDecimal("1.50"), response.desconto());
        assertEquals(new BigDecimal("2.00"), response.frete());
        assertEquals(new BigDecimal("20.50"), response.total());
        assertEquals("presente", response.observacao());
        assertEquals(2, response.itens().getFirst().quantidade());
        verify(estoque).baixarEstoque(produto, null, 2, cliente, "Pedido " + response.numero());
    }

    @Test
    void criarCalculaDescontoDoCupomEIgnoraDescontoInformado() {
        when(usuarios.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtos.findById(9L)).thenReturn(Optional.of(produto));
        when(cupons.findByCodigoIgnoreCase("BEMVINDO")).thenReturn(Optional.of(cupom("BEMVINDO", "10")));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponse response = service.criar(new PedidoRequest(1L, null, " BEMVINDO ",
                new BigDecimal("15.00"), BigDecimal.ZERO, null,
                List.of(new PedidoItemRequest(9L, null, 2))));

        assertEquals("BEMVINDO", response.cupom());
        assertEquals(new BigDecimal("2.00"), response.desconto());
        assertEquals(new BigDecimal("18.00"), response.total());
    }

    @Test
    void criarRejeitaPedidoSemItensEFreteNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(new PedidoRequest(1L, null, null, null, null, null, List.of())));
        when(usuarios.findById(1L)).thenReturn(Optional.of(cliente));

        assertThrows(IllegalArgumentException.class,
                () -> service.criar(new PedidoRequest(1L, null, null, null, new BigDecimal("-0.01"), null,
                        List.of(new PedidoItemRequest(9L, null, 1)))));
        verifyNoInteractions(produtos, estoque);
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

    private static Cupom cupom(String codigo, String percentual) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setPercentualDesconto(new BigDecimal(percentual));
        return cupom;
    }
}
