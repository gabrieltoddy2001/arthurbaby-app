package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.AvaliacaoRequest;
import br.com.arthurbaby.dto.AvaliacaoResponse;
import br.com.arthurbaby.entity.Avaliacao;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.AvaliacaoRepository;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvaliacaoServiceTest {
    private AvaliacaoRepository avaliacoes;
    private ProdutoRepository produtos;
    private UsuarioRepository usuarios;
    private PedidoRepository pedidos;
    private AvaliacaoService service;
    private Produto produto;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        avaliacoes = mock(AvaliacaoRepository.class);
        produtos = mock(ProdutoRepository.class);
        usuarios = mock(UsuarioRepository.class);
        pedidos = mock(PedidoRepository.class);
        service = new AvaliacaoService(avaliacoes, produtos, usuarios, pedidos);

        produto = new Produto();
        produto.setId(1L);
        cliente = new Usuario();
        cliente.setId(8L);
        cliente.setNomeCompleto("Maria Silva");
        when(produtos.findById(1L)).thenReturn(Optional.of(produto));
        when(produtos.existsById(1L)).thenReturn(true);
        when(usuarios.findById(8L)).thenReturn(Optional.of(cliente));
        // Cenario padrao: cliente recebeu o produto e ainda nao avaliou
        when(pedidos.existsByClienteIdAndStatusAndItensProdutoId(8L, PedidoStatus.ENTREGUE, 1L)).thenReturn(true);
        when(avaliacoes.existsByClienteIdAndProdutoId(8L, 1L)).thenReturn(false);
        when(avaliacoes.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao a = invocation.getArgument(0);
            a.setId(1L);
            a.setCriadoEm(LocalDateTime.of(2026, 10, 19, 10, 0));
            return a;
        });
    }

    @Test
    void avaliarCriaAvaliacaoEAtualizaMediaDoProduto() {
        when(avaliacoes.calcularMediaAvaliacoesByProdutoId(1L)).thenReturn(4.5);

        AvaliacaoResponse r = service.avaliar(1L, new AvaliacaoRequest(8L, 5, "  Tecido muito macio, amei!  "), cliente);

        assertEquals(1L, r.id());
        assertEquals(5, r.nota());
        assertEquals("Tecido muito macio, amei!", r.comentario());
        assertEquals("Maria Silva", r.clienteNome());
        assertEquals(new BigDecimal("4.5"), produto.getAvaliacao());
        verify(produtos).save(produto);
    }

    @Test
    void avaliarSemClienteIdUsaODonoDoTokenEComentarioVazioViraNulo() {
        when(avaliacoes.calcularMediaAvaliacoesByProdutoId(1L)).thenReturn(3.0);

        AvaliacaoResponse r = service.avaliar(1L, new AvaliacaoRequest(null, 3, "   "), cliente);

        assertEquals("Maria Silva", r.clienteNome());
        assertNull(r.comentario());
    }

    @Test
    void avaliarRejeitaNotaForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class, () -> service.avaliar(1L, new AvaliacaoRequest(8L, 0, null), cliente));
        assertThrows(IllegalArgumentException.class, () -> service.avaliar(1L, new AvaliacaoRequest(8L, 6, null), cliente));
        assertThrows(IllegalArgumentException.class, () -> service.avaliar(1L, new AvaliacaoRequest(8L, null, null), cliente));
        verify(avaliacoes, never()).save(any(Avaliacao.class));
    }

    @Test
    void avaliarRejeitaAvaliacaoEmNomeDeOutroCliente() {
        assertThrows(AccessDeniedException.class, () -> service.avaliar(1L, new AvaliacaoRequest(9L, 5, null), cliente));
        verify(avaliacoes, never()).save(any(Avaliacao.class));
    }

    @Test
    void avaliarRejeitaProdutoInexistente() {
        when(produtos.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.avaliar(99L, new AvaliacaoRequest(8L, 5, null), cliente));
    }

    @Test
    void avaliarRejeitaSegundaAvaliacaoDoMesmoProduto() {
        when(avaliacoes.existsByClienteIdAndProdutoId(8L, 1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.avaliar(1L, new AvaliacaoRequest(8L, 5, null), cliente));

        assertEquals("Voce ja avaliou este produto", ex.getMessage());
        verify(avaliacoes, never()).save(any(Avaliacao.class));
    }

    @Test
    void avaliarExigePedidoEntregueComOProduto() {
        when(pedidos.existsByClienteIdAndStatusAndItensProdutoId(8L, PedidoStatus.ENTREGUE, 1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.avaliar(1L, new AvaliacaoRequest(8L, 5, null), cliente));
        verify(avaliacoes, never()).save(any(Avaliacao.class));
    }

    @Test
    void listarDevolveAvaliacoesDoProdutoOu404() {
        Avaliacao a = new Avaliacao();
        a.setId(2L);
        a.setNota(4);
        a.setCliente(cliente);
        when(avaliacoes.findByProdutoIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(a));

        List<AvaliacaoResponse> lista = service.listar(1L);

        assertEquals(1, lista.size());
        assertEquals(4, lista.getFirst().nota());
        assertEquals("Maria Silva", lista.getFirst().clienteNome());
        assertThrows(NoSuchElementException.class, () -> service.listar(99L));
    }

    @Test
    void arredondarMediaUsaUmaCasaDecimal() {
        assertEquals(new BigDecimal("4.7"), AvaliacaoService.arredondarMedia(4.666));
        assertEquals(new BigDecimal("0.0"), AvaliacaoService.arredondarMedia(null));
    }
}
