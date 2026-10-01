package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.MovimentacaoEstoqueAdminResponse;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EstoqueService {
    private final ProdutoVariacaoRepository variacoes;
    private final MovimentacaoEstoqueRepository movimentos;
    public EstoqueService(ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos) {
        this.variacoes = variacoes;
        this.movimentos = movimentos;
    }
    @Transactional
    public MovimentacaoEstoque baixarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) return null;
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        int anterior = variacao.getEstoqueAtual();
        if (anterior < quantidade) throw new IllegalArgumentException("Estoque insuficiente para SKU " + variacao.getSku());
        variacao.setEstoqueAtual(anterior - quantidade);
        variacoes.save(variacao);
        return registrar(produto, variacao, usuario, MovimentoEstoqueTipo.SAIDA, quantidade, anterior, variacao.getEstoqueAtual(), observacao);
    }
    @Transactional
    public MovimentacaoEstoque adicionarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Variacao e obrigatoria para entrada de estoque");
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        int anterior = variacao.getEstoqueAtual();
        variacao.setEstoqueAtual(anterior + quantidade);
        variacoes.save(variacao);
        return registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ENTRADA, quantidade, anterior, variacao.getEstoqueAtual(), observacao);
    }
    /** Correcao manual: define o estoque para {@code novaQuantidade}; a movimentacao guarda a diferenca absoluta. */
    @Transactional
    public MovimentacaoEstoque ajustarEstoque(Produto produto, ProdutoVariacao variacao, int novaQuantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Variacao e obrigatoria para ajuste de estoque");
        if (novaQuantidade < 0) throw new IllegalArgumentException("Estoque nao pode ser negativo");
        int anterior = variacao.getEstoqueAtual();
        variacao.setEstoqueAtual(novaQuantidade);
        variacoes.save(variacao);
        return registrar(produto, variacao, usuario, MovimentoEstoqueTipo.AJUSTE, Math.abs(novaQuantidade - anterior), anterior, novaQuantidade, observacao);
    }
    /** Devolve ao estoque os itens de um pedido cancelado. Itens sem variacao nao baixam estoque, portanto nao sao estornados. */
    @Transactional
    public void estornarEstoque(Pedido pedido, Usuario usuario, String observacao) {
        for (PedidoItem item : pedido.getItens()) {
            if (item.getVariacao() == null) continue;
            estornarEstoque(item.getProduto(), item.getVariacao(), item.getQuantidade(), usuario, observacao);
        }
    }
    /** Devolucao manual ao estoque (ex.: troca/devolucao fora de um cancelamento de pedido). */
    @Transactional
    public MovimentacaoEstoque estornarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Variacao e obrigatoria para estorno de estoque");
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        int anterior = variacao.getEstoqueAtual();
        variacao.setEstoqueAtual(anterior + quantidade);
        variacoes.save(variacao);
        return registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ESTORNO, quantidade, anterior, variacao.getEstoqueAtual(), observacao);
    }

    /** Historico completo para o painel admin, da movimentacao mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueAdminResponse> listarMovimentacoes() {
        return movimentos.findAllByOrderByCriadoEmDesc().stream().map(MovimentacaoEstoqueAdminResponse::de).toList();
    }

    /** Operacao manual do painel admin sobre uma variacao (ENTRADA, SAIDA, AJUSTE ou ESTORNO); devolve a movimentacao registrada. */
    @Transactional
    public MovimentacaoEstoqueResponse movimentarVariacao(Long variacaoId, MovimentoEstoqueTipo tipo, Integer quantidade,
                                                          Usuario usuario, String observacao) {
        if (quantidade == null) throw new IllegalArgumentException("Quantidade e obrigatoria");
        ProdutoVariacao variacao = variacoes.findById(variacaoId)
                .orElseThrow(() -> new NoSuchElementException("Variacao nao encontrada"));
        Produto produto = variacao.getProduto();
        MovimentacaoEstoque mov = switch (tipo) {
            case ENTRADA -> adicionarEstoque(produto, variacao, quantidade, usuario, observacao);
            case SAIDA -> baixarEstoque(produto, variacao, quantidade, usuario, observacao);
            case AJUSTE -> ajustarEstoque(produto, variacao, quantidade, usuario, observacao);
            case ESTORNO -> estornarEstoque(produto, variacao, quantidade, usuario, observacao);
            default -> throw new IllegalArgumentException("Operacao de estoque nao suportada: " + tipo);
        };
        return MovimentacaoEstoqueResponse.de(mov);
    }

    public MovimentacaoEstoque registrar(Produto produto, ProdutoVariacao variacao, Usuario usuario, MovimentoEstoqueTipo tipo,
                                         int quantidade, Integer anterior, Integer posterior, String observacao) {
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setVariacao(variacao);
        mov.setUsuario(usuario);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setEstoqueAnterior(anterior);
        mov.setEstoquePosterior(posterior);
        mov.setObservacao(observacao);
        return movimentos.save(mov);
    }
}
