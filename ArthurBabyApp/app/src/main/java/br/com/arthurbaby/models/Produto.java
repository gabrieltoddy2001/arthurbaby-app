package br.com.arthurbaby.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {

    private Long id;
    private String nome;
    private String descricao;
    /** Preço efetivamente cobrado (promocional, quando houver). */
    private BigDecimal preco;
    private String imagemUrl;
    private int estoque;
    private String marca;

    // Campos vindos do back
    private double avaliacao;
    /** Preço "de" (riscado) — só preenchido quando há preço promocional real menor que o normal. */
    private BigDecimal precoOriginal;
    private boolean promocao;
    /** ATIVO, INATIVO ou ESGOTADO. */
    private String status;
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
        this.preco = preco != null ? preco : BigDecimal.ZERO;
        this.imagemUrl = imagemUrl;
        this.estoque = estoque;
        this.marca = marca;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco != null ? preco : BigDecimal.ZERO; }
    public String getImagemUrl() { return imagemUrl; }
    public int getEstoque() { return estoque; }
    public String getMarca() { return marca; }
    public double getAvaliacao() { return avaliacao; }
    public BigDecimal getPrecoOriginal() { return precoOriginal; }
    public boolean isPromocao() { return promocao; }
    public String getStatus() { return status; }
    public List<String> getImagens() { return imagens; }
    public List<VariacaoReal> getVariacoes() { return variacoes; }

    public void setAvaliacao(double avaliacao) { this.avaliacao = avaliacao; }
    public void setPrecoOriginal(BigDecimal precoOriginal) { this.precoOriginal = precoOriginal; }
    public void setPromocao(boolean promocao) { this.promocao = promocao; }
    public void setStatus(String status) { this.status = status; }
    public void setImagens(List<String> imagens) { this.imagens = imagens != null ? imagens : new ArrayList<>(); }
    public void setVariacoes(List<VariacaoReal> variacoes) { this.variacoes = variacoes != null ? variacoes : new ArrayList<>(); }

    /** Há um preço "de" maior que o preço atual para exibir riscado. */
    public boolean temDesconto() {
        return precoOriginal != null && precoOriginal.compareTo(getPreco()) > 0;
    }

    /**
     * Esgotado quando o back marca o produto como ESGOTADO ou, se as variações
     * já foram carregadas (tela de detalhe), quando nenhuma tem estoque.
     */
    public boolean isEsgotado() {
        if ("ESGOTADO".equals(status)) return true;
        if (variacoes != null && !variacoes.isEmpty()) {
            for (VariacaoReal v : variacoes) {
                if (v.estoqueAtual > 0) return false;
            }
            return true;
        }
        return estoque == 0;
    }

    public boolean temEstoque() { return !isEsgotado(); }

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
