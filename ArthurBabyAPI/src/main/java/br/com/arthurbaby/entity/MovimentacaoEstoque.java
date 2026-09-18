package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class MovimentacaoEstoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Produto produto;
    @ManyToOne private ProdutoVariacao variacao;
    @ManyToOne private Usuario usuario;
    @Enumerated(EnumType.STRING) private MovimentoEstoqueTipo tipo;
    private int quantidade;
    private Integer estoqueAnterior;
    private Integer estoquePosterior;
    private String observacao;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }
}
