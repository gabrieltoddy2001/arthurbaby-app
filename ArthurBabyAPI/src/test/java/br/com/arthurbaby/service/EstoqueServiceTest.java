package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Enums.ProdutoStatus;
import br.com.arthurbaby.repository.MovimentacaoEstoqueRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstoqueServiceTest {
    private MovimentacaoEstoqueRepository movimentos;
    private ProdutoVariacaoRepository variacoes;
    private EstoqueService service;
    private Produto produto;
    private ProdutoVariacao variacao;

    @BeforeEach
    void setUp() {
        movimentos = mock(MovimentacaoEstoqueRepository.class);
        variacoes = mock(ProdutoVariacaoRepository.class);
        service = new EstoqueService(variacoes, movimentos);
        produto = new Produto();
        produto.setNome("Body Manga Curta");
        variacao = new ProdutoVariacao();
        variacao.setId(1L);
        variacao.setSku("BODY-RN-BR");
        Tamanho rn = new Tamanho();
        rn.setNome("RN");
        variacao.setTamanho(rn);
        variacao.setEstoqueAtual(10);
    }

    private MovimentacaoEstoque ultimaMovimentacao() {
        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentos, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void baixarEstoqueUsaInstrucaoAtomicaERegistraSaida() {
        when(variacoes.baixarSeDisponivel(1L, 3)).thenReturn(1);
        when(variacoes.saldoAtual(1L)).thenReturn(7);

        service.baixarEstoque(produto, variacao, 3, null, "Pedido AB-1");

        assertEquals(7, variacao.getEstoqueAtual());
        MovimentacaoEstoque mov = ultimaMovimentacao();
        assertEquals(MovimentoEstoqueTipo.SAIDA, mov.getTipo());
        assertEquals(10, mov.getEstoqueAnterior());
        assertEquals(7, mov.getEstoquePosterior());
    }

    @Test
    void baixarEstoqueInsuficienteInformaProdutoEDisponivel() {
        when(variacoes.baixarSeDisponivel(1L, 5)).thenReturn(0);
        when(variacoes.saldoAtual(1L)).thenReturn(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.baixarEstoque(produto, variacao, 5, null, null));

        assertEquals("Estoque insuficiente para Body Manga Curta (RN): disponível 2 unidades, solicitado 5.", ex.getMessage());
        verifyNoInteractions(movimentos);
    }

    @Test
    void baixarEstoqueZeradoInformaProdutoSemEstoque() {
        when(variacoes.baixarSeDisponivel(1L, 1)).thenReturn(0);
        when(variacoes.saldoAtual(1L)).thenReturn(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.baixarEstoque(produto, variacao, 1, null, null));

        assertTrue(ex.getMessage().startsWith("Produto sem estoque: Body Manga Curta (RN)."), ex.getMessage());
        assertFalse(ex.getMessage().contains("\""), "mensagem nao pode ter aspas (o app le o JSON ate a proxima aspa)");
    }

    @Test
    void baixarEstoqueExigeVariacao() {
        assertThrows(IllegalArgumentException.class, () -> service.baixarEstoque(produto, null, 1, null, null));
        verifyNoInteractions(variacoes);
    }

    @Test
    void adicionarEstoqueSomaERegistraEntrada() {
        when(variacoes.saldoAtual(1L)).thenReturn(15);

        service.adicionarEstoque(produto, variacao, 5, null, "reposicao");

        verify(variacoes).somarEstoque(1L, 5);
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
        verifyNoInteractions(variacoes);
    }

    @Test
    void ajustarEstoqueDefineNovoValorERegistraDiferenca() {
        when(variacoes.saldoAtual(1L)).thenReturn(10);

        service.ajustarEstoque(produto, variacao, 4, null, "contagem");

        verify(variacoes).definirEstoque(1L, 4);
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
        when(variacoes.saldoAtual(1L)).thenReturn(13);
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

        verify(variacoes).somarEstoque(1L, 3);
        assertEquals(13, variacao.getEstoqueAtual());
        verify(movimentos, times(1)).save(any(MovimentacaoEstoque.class));
        assertEquals(MovimentoEstoqueTipo.ESTORNO, ultimaMovimentacao().getTipo());
    }

    @Test
    void produtoAtivoSemSaldoViraEsgotadoEVoltaAoReceberEstoque() {
        produto.setId(9L);
        produto.setStatus(ProdutoStatus.ATIVO);

        when(variacoes.saldoDoProduto(9L)).thenReturn(0L);
        service.atualizarSituacaoProduto(produto);
        assertEquals(ProdutoStatus.ESGOTADO, produto.getStatus());

        when(variacoes.saldoDoProduto(9L)).thenReturn(4L);
        service.atualizarSituacaoProduto(produto);
        assertEquals(ProdutoStatus.ATIVO, produto.getStatus());
    }

    @Test
    void produtoInativoNaoMudaDeStatus() {
        produto.setId(9L);
        produto.setStatus(ProdutoStatus.INATIVO);
        service.atualizarSituacaoProduto(produto);
        assertEquals(ProdutoStatus.INATIVO, produto.getStatus());
        verifyNoInteractions(variacoes);
    }

    @Test
    void baixaQueZeraOEstoqueMarcaProdutoComoEsgotado() {
        produto.setId(9L);
        when(variacoes.baixarSeDisponivel(1L, 10)).thenReturn(1);
        when(variacoes.saldoAtual(1L)).thenReturn(0);
        when(variacoes.saldoDoProduto(9L)).thenReturn(0L);

        service.baixarEstoque(produto, variacao, 10, null, null);

        assertEquals(ProdutoStatus.ESGOTADO, produto.getStatus());
    }
}
