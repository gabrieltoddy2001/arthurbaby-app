package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class Marca {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String nome;
    @Enumerated(EnumType.STRING) private CategoriaStatus status = CategoriaStatus.ATIVA;
}
