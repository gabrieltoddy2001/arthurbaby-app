package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.CorStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
public class Cor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String nome;
    @Column(length = 7) private String codigoHex;
    private int ordemExibicao;
    @Enumerated(EnumType.STRING) private CorStatus status = CorStatus.ATIVA;
}
