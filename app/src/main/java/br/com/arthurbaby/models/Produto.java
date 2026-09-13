package br.com.arthurbaby.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Produto implements Serializable {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String imagemUrl;
    private int estoque;
    private String marca;

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

    public boolean isEsgotado() { return estoque == 0; }
    public boolean temEstoque() { return estoque != 0; }
}