package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.repository.MovimentacaoEstoqueRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstoqueServiceTest {
    private MovimentacaoEstoqueRepository movimentos;
    private EstoqueService service;
    private Produto produto;
    private ProdutoVariacao variacao;

    @BeforeEach
    void setUp() {
        movimentos = mock(MovimentacaoEstoqueRepository.class);
        service = new EstoqueService(mock(ProdutoVariacaoRepository.class), movimentos);
        produto = new Produto();
        variacao = new ProdutoVariacao();
        variacao.setEstoqueAtual(10);
    }

    private MovimentacaoEstoque ultimaMovimentacao() {
        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentos, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void adicionarEstoqueSomaERegistraEntrada() {
        service.adicionarEstoque(produto, variacao, 5, null, "reposicao");
        assertEquals(15, variacao.getEstoqueAtual());
        MovimentacaoEstoque mov = ultimaMovimentacao();
        assertEquals(MovimentoEstoqueTipo.ENTRADA, mov.getTipo());
        assertEquals(10, mov.getEstoqueAnterior());
        assertEquals(15, mov.getEstoquePosterior());
    }

    @Test
    void adicionarEstoqueRejeitaQuantidadeInvalida() {
        assertThrows(IllegalArgumentException.class, () -> service.adicionarEstoque(produto, variacao, 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.adicionarEstoque(produto, null, 1, null, null));
        assertEquals(10, variacao.getEstoqueAtual());
    }

    @Test
    void ajustarEstoqueDefineNovoValorERegistraDiferenca() {
        service.ajustarEstoque(produto, variacao, 4, null, "contagem");
        assertEquals(4, variacao.getEstoqueAtual());
        MovimentacaoEstoque mov = ultimaMovimentacao();
        assertEquals(MovimentoEstoqueTipo.AJUSTE, mov.getTipo());
        assertEquals(6, mov.getQuantidade());
        assertEquals(10, mov.getEstoqueAnterior());
        assertEquals(4, mov.getEstoquePosterior());
    }

    @Test
    void ajustarEstoqueRejeitaNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.ajustarEstoque(produto, variacao, -1, null, null));
        assertEquals(10, variacao.getEstoqueAtual());
    }

    @Test
    void estornarEstoqueDevolveItensComVariacaoEIgnoraOsSemVariacao() {
        Pedido pedido = new Pedido();
        PedidoItem comVariacao = new PedidoItem();
        comVariacao.setProduto(produto);
        comVariacao.setVariacao(variacao);
        comVariacao.setQuantidade(3);
        PedidoItem semVariacao = new PedidoItem();
        semVariacao.setProduto(produto);
        semVariacao.setQuantidade(2);
        pedido.getItens().add(comVariacao);
        pedido.getItens().add(semVariacao);

        service.estornarEstoque(pedido, null, "cancelamento");

        assertEquals(13, variacao.getEstoqueAtual());
        verify(movimentos, times(1)).save(any(MovimentacaoEstoque.class));
        assertEquals(MovimentoEstoqueTipo.ESTORNO, ultimaMovimentacao().getTipo());
    }
}
