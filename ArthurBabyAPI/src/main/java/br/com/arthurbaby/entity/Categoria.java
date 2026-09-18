package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne private Categoria categoriaPai;
    @Column(nullable = false, unique = true, length = 100) private String nome;
    @Column(length = 500) private String descricao;
    @Column(length = 500) private String imagemUrl;
    private int ordemExibicao;
    @Enumerated(EnumType.STRING) private CategoriaStatus status = CategoriaStatus.ATIVA;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); atualizadoEm = criadoEm; }
    @PreUpdate void preUpdate() { atualizadoEm = LocalDateTime.now(); }
}
