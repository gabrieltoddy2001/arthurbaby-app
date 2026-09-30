package br.com.arthurbaby.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class ProdutoImagem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    // WRITE_ONLY: aceito na entrada (CRUD admin envia { "id": N }), mas omitido na saida para evitar referencia circular no JSON
    @ManyToOne(optional = false) @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private Produto produto;
    @Column(nullable = false, length = 500) private String url;
    private String descricao;
    private int ordemExibicao;
    private boolean principal;
}
