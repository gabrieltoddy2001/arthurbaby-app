package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.MovimentacaoEstoqueAdminResponse;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Enums.ProdutoStatus;
import br.com.arthurbaby.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Estoque por variação (tamanho/cor/modelo).
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>A baixa acontece na criação do pedido; o cancelamento devolve (estorno). Mudanças de status não mexem no estoque.</li>
 *   <li>Todas as alterações de saldo são instruções atômicas no banco ({@link ProdutoVariacaoRepository#baixarSeDisponivel}),
 *       então duas vendas simultâneas da última unidade não passam ambas. Depois da instrução, a linha fica bloqueada
 *       pela transação até o commit; por isso o saldo relido em seguida é confiável.</li>
 *   <li>Após cada movimentação o status do produto é recalculado: ATIVO sem saldo vira ESGOTADO e ESGOTADO com saldo
 *       volta a ATIVO. Produtos INATIVOS nunca são alterados.</li>
 * </ul>
 */
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
        if (variacao == null) throw new IllegalArgumentException(MensagensLoja.selecioneVariacao(produto));
        validarQuantidade(quantidade);
        if (variacoes.baixarSeDisponivel(variacao.getId(), quantidade) == 0) {
            Integer disponivel = variacoes.saldoAtual(variacao.getId());
            throw new IllegalArgumentException(MensagensLoja.estoqueInsuficiente(produto, variacao,
                    disponivel == null ? 0 : disponivel, quantidade));
        }
        int posterior = sincronizar(variacao);
        MovimentacaoEstoque mov = registrar(produto, variacao, usuario, MovimentoEstoqueTipo.SAIDA, quantidade,
                posterior + quantidade, posterior, observacao);
        atualizarSituacaoProduto(produto);
        return mov;
    }

    @Transactional
    public MovimentacaoEstoque adicionarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Informe a variação (tamanho/cor/modelo) para a entrada de estoque.");
        validarQuantidade(quantidade);
        variacoes.somarEstoque(variacao.getId(), quantidade);
        int posterior = sincronizar(variacao);
        MovimentacaoEstoque mov = registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ENTRADA, quantidade,
                posterior - quantidade, posterior, observacao);
        atualizarSituacaoProduto(produto);
        return mov;
    }

    /** Correção manual: define o estoque para {@code novaQuantidade}; a movimentação guarda a diferença absoluta. */
    @Transactional
    public MovimentacaoEstoque ajustarEstoque(Produto produto, ProdutoVariacao variacao, int novaQuantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Informe a variação (tamanho/cor/modelo) para o ajuste de estoque.");
        if (novaQuantidade < 0) throw new IllegalArgumentException("O estoque não pode ficar negativo.");
        Integer lido = variacoes.saldoAtual(variacao.getId());
        int anterior = lido == null ? variacao.getEstoqueAtual() : lido;
        variacoes.definirEstoque(variacao.getId(), novaQuantidade);
        variacao.setEstoqueAtual(novaQuantidade);
        MovimentacaoEstoque mov = registrar(produto, variacao, usuario, MovimentoEstoqueTipo.AJUSTE,
                Math.abs(novaQuantidade - anterior), anterior, novaQuantidade, observacao);
        atualizarSituacaoProduto(produto);
        return mov;
    }

    /** Devolve ao estoque os itens de um pedido cancelado. Itens sem variação não baixaram estoque, portanto não são estornados. */
    @Transactional
    public void estornarEstoque(Pedido pedido, Usuario usuario, String observacao) {
        for (PedidoItem item : pedido.getItens()) {
            if (item.getVariacao() == null) continue;
            estornarEstoque(item.getProduto(), item.getVariacao(), item.getQuantidade(), usuario, observacao);
        }
    }

    /** Devolução manual ao estoque (ex.: troca/devolução fora de um cancelamento de pedido). */
    @Transactional
    public MovimentacaoEstoque estornarEstoque(Produto produto, ProdutoVariacao variacao, int quantidade, Usuario usuario, String observacao) {
        if (variacao == null) throw new IllegalArgumentException("Informe a variação (tamanho/cor/modelo) para o estorno de estoque.");
        validarQuantidade(quantidade);
        variacoes.somarEstoque(variacao.getId(), quantidade);
        int posterior = sincronizar(variacao);
        MovimentacaoEstoque mov = registrar(produto, variacao, usuario, MovimentoEstoqueTipo.ESTORNO, quantidade,
                posterior - quantidade, posterior, observacao);
        atualizarSituacaoProduto(produto);
        return mov;
    }

    /**
     * Marca o produto como ESGOTADO quando nenhuma variação ativa tem saldo e volta para ATIVO quando
     * o saldo retorna. Produtos INATIVOS (desativados pela loja) não são alterados.
     */
    @Transactional
    public void atualizarSituacaoProduto(Produto produto) {
        if (produto == null || produto.getId() == null || produto.getStatus() == ProdutoStatus.INATIVO) return;
        long saldo = variacoes.saldoDoProduto(produto.getId());
        if (produto.getStatus() == ProdutoStatus.ATIVO && saldo <= 0) produto.setStatus(ProdutoStatus.ESGOTADO);
        else if (produto.getStatus() == ProdutoStatus.ESGOTADO && saldo > 0) produto.setStatus(ProdutoStatus.ATIVO);
    }

    /** Histórico completo para o painel admin, da movimentação mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueAdminResponse> listarMovimentacoes() {
        return movimentos.findAllByOrderByCriadoEmDesc().stream().map(MovimentacaoEstoqueAdminResponse::de).toList();
    }

    /** Operação manual do painel admin sobre uma variação (ENTRADA, SAIDA, AJUSTE ou ESTORNO); devolve a movimentação registrada. */
    @Transactional
    public MovimentacaoEstoqueResponse movimentarVariacao(Long variacaoId, MovimentoEstoqueTipo tipo, Integer quantidade,
                                                          Usuario usuario, String observacao) {
        if (quantidade == null) throw new IllegalArgumentException("Informe a quantidade.");
        ProdutoVariacao variacao = variacoes.findById(variacaoId)
                .orElseThrow(() -> new NoSuchElementException("Variação não encontrada."));
        Produto produto = variacao.getProduto();
        MovimentacaoEstoque mov = switch (tipo) {
            case ENTRADA -> adicionarEstoque(produto, variacao, quantidade, usuario, observacao);
            case SAIDA -> baixarEstoque(produto, variacao, quantidade, usuario, observacao);
            case AJUSTE -> ajustarEstoque(produto, variacao, quantidade, usuario, observacao);
            case ESTORNO -> estornarEstoque(produto, variacao, quantidade, usuario, observacao);
            default -> throw new IllegalArgumentException("Operação de estoque não suportada: " + tipo + ".");
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

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
    }

    /** Relê o saldo do banco após a instrução atômica e atualiza o objeto em memória (a linha está bloqueada pela transação). */
    private int sincronizar(ProdutoVariacao variacao) {
        Integer saldo = variacoes.saldoAtual(variacao.getId());
        int valor = saldo == null ? variacao.getEstoqueAtual() : saldo;
        variacao.setEstoqueAtual(valor);
        return valor;
    }

    /** Lista de variações para uso em mensagens (tamanho / cor / modelo). */
    static String descreverVariacao(ProdutoVariacao v) {
        if (v == null) return null;
        List<String> partes = new ArrayList<>();
        if (v.getTamanho() != null) partes.add(v.getTamanho().getNome());
        if (v.getCor() != null) partes.add(v.getCor().getNome());
        if (v.getModelo() != null) partes.add(v.getModelo().getNome());
        return partes.isEmpty() ? v.getSku() : String.join(" / ", partes);
    }
}
