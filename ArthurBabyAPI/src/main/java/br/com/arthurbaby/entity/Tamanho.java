package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.AtivoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class Tamanho {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 30) private String nome;
    private int ordemExibicao;
    @Enumerated(EnumType.STRING) private AtivoStatus status = AtivoStatus.ATIVO;
}
