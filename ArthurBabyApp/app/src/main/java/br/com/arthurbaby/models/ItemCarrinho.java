package br.com.arthurbaby.models;

import java.io.Serializable;

public class ItemCarrinho implements Serializable {

    private Produto produto;
    private int quantidade;
    private Variacao variacao;
    private Long variacaoId;

    /** Construtor vazio — necessário para o Gson. */
    public ItemCarrinho() {}

    public ItemCarrinho(Produto produto, int quantidade) {
        this(produto, quantidade, null, null);
    }

    public ItemCarrinho(Produto produto, int quantidade, Variacao variacao) {
        this(produto, quantidade, variacao, null);
    }

    public ItemCarrinho(Produto produto, int quantidade, Variacao variacao, Long variacaoId) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.variacao = variacao;
        this.variacaoId = variacaoId;
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public Variacao getVariacao() { return variacao; }
    public Long getVariacaoId() { return variacaoId; }

    public void setProduto(Produto produto) { this.produto = produto; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setVariacao(Variacao variacao) { this.variacao = variacao; }
    public void setVariacaoId(Long variacaoId) { this.variacaoId = variacaoId; }

    /**
     * Preço próprio da variação escolhida. O back (PedidoService) cobra o preço da variação
     * quando ela tem um; sem isso o total do carrinho divergia do total do pedido.
     */
    private java.math.BigDecimal precoVariacao;

    public void setPrecoVariacao(java.math.BigDecimal precoVariacao) { this.precoVariacao = precoVariacao; }

    /** Preço unitário efetivo: o da variação, se houver; senão o do produto. */
    public java.math.BigDecimal getPrecoUnitario() {
        return precoVariacao != null ? precoVariacao : produto.getPreco();
    }

    public double getSubtotal() {
        return getPrecoUnitario().doubleValue() * quantidade;
    }

    /** Mesmo produto e mesma variação = mesmo item no carrinho. */
    public boolean mesmoItem(Long produtoId, Long outraVariacaoId) {
        return produto.getId().equals(produtoId)
                && java.util.Objects.equals(variacaoId, outraVariacaoId);
    }
}