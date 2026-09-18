package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {
    private final ProdutoVariacaoRepository variacoes;
    private final MovimentacaoEstoqueRepository movimentos;
    public EstoqueService(ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos) {
        this.variacoes = variacoes;
        this.movimentos = movimentos;
    }
    @Transactional
    public void baixarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) return;
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        int anterior = variacao.getEstoqueAtual();
        if (anterior < quantidade) throw new IllegalArgumentException("Estoque insuficiente para SKU " + variacao.getSku());
        variacao.setEstoqueAtual(anterior - quantidade);
        variacoes.save(variacao);
        registrar(produto, variacao, usuario, MovimentoEstoqueTipo.SAIDA, quantidade, anterior, variacao.getEstoqueAtual(), observacao);
    }
    public void registrar(Produto produto, ProdutoVariacao variacao, Usuario usuario, MovimentoEstoqueTipo tipo,
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
        movimentos.save(mov);
    }
}
