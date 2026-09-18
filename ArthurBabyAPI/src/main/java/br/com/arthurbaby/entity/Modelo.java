package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.AtivoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class Modelo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String nome;
    @Enumerated(EnumType.STRING) private AtivoStatus status = AtivoStatus.ATIVO;
}
