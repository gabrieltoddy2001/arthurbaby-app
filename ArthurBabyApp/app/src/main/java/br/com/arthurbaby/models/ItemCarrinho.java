package br.com.arthurbaby.models;

import java.io.Serializable;

public class ItemCarrinho implements Serializable {

    private Produto produto;
    private int quantidade;
    private Variacao variacao;

    public ItemCarrinho(Produto produto, int quantidade) {
        this(produto, quantidade, null);
    }

    public ItemCarrinho(Produto produto, int quantidade, Variacao variacao) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.variacao = variacao;
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public Variacao getVariacao() { return variacao; }

    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getSubtotal() {
        return produto.getPreco().doubleValue() * quantidade;
    }
}