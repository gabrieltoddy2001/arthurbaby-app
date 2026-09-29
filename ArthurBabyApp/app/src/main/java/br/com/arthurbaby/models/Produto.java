package br.com.arthurbaby.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String imagemUrl;
    private int estoque;
    private String marca;

    // Novos campos (vindos do back)
    private int avaliacao;
    private List<String> imagens = new ArrayList<>();
    private List<VariacaoReal> variacoes = new ArrayList<>();

    public Produto(Long id, String nome, String descricao, BigDecimal preco, String imagemUrl) {
        this(id, nome, descricao, preco, imagemUrl, 10, "ArthurBaby");
    }

    public Produto(Long id, String nome, String descricao, BigDecimal preco,
                   String imagemUrl, int estoque) {
        this(id, nome, descricao, preco, imagemUrl, estoque, "ArthurBaby");
    }

    public Produto(Long id, String nome, String descricao, BigDecimal preco,
                   String imagemUrl, int estoque, String marca) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.imagemUrl = imagemUrl;
        this.estoque = estoque;
        this.marca = marca;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public String getImagemUrl() { return imagemUrl; }
    public int getEstoque() { return estoque; }
    public String getMarca() { return marca; }
    public int getAvaliacao() { return avaliacao; }
    public List<String> getImagens() { return imagens; }
    public List<VariacaoReal> getVariacoes() { return variacoes; }

    public void setAvaliacao(int avaliacao) { this.avaliacao = avaliacao; }
    public void setImagens(List<String> imagens) { this.imagens = imagens != null ? imagens : new ArrayList<>(); }
    public void setVariacoes(List<VariacaoReal> variacoes) { this.variacoes = variacoes != null ? variacoes : new ArrayList<>(); }

    public boolean isEsgotado() { return estoque == 0; }
    public boolean temEstoque() { return estoque != 0; }

    /**
     * Representa uma variação real do backend (com ID).
     */
    public static class VariacaoReal implements Serializable {
        public Long id;
        public String sku;
        public String tamanho;
        public String cor;
        public String modelo;
        public BigDecimal preco;
        public int estoqueAtual;

        public VariacaoReal(Long id, String sku, String tamanho, String cor,
                            String modelo, BigDecimal preco, int estoqueAtual) {
            this.id = id;
            this.sku = sku;
            this.tamanho = tamanho;
            this.cor = cor;
            this.modelo = modelo;
            this.preco = preco;
            this.estoqueAtual = estoqueAtual;
        }
    }
}