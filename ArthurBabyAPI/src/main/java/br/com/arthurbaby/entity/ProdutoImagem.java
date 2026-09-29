package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class ProdutoImagem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JsonIgnore private Produto produto;
    @Column(nullable = false, length = 500) private String url;
    private String descricao;
    private int ordemExibicao;
    private boolean principal;
}
