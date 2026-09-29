package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Entity
public class Cupom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String codigo;
    @Column(nullable = false, precision = 5, scale = 2) private BigDecimal percentualDesconto;
    private boolean ativo = true;
    private LocalDateTime validoAte;

    public boolean estaValido() {
        return ativo && (validoAte == null || validoAte.isAfter(LocalDateTime.now()));
    }
}
