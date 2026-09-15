package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "produto_id"}))
public class Favorito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Usuario cliente;
    @ManyToOne(optional = false) private Produto produto;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }
}
