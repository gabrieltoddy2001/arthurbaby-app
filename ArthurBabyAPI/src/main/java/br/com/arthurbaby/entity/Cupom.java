package br.com.arthurbaby.entity;

import br.com.arthurbaby.entity.Enums.TipoCupom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cupom de desconto cadastrado pelo admin. {@code valor} e o percentual (PERCENTUAL) ou o valor em reais
 * (VALOR_FIXO); FRETE_GRATIS nao usa {@code valor}.
 */
@Getter @Setter @Entity
public class Cupom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String codigo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoCupom tipo = TipoCupom.PERCENTUAL;
    @Column(precision = 12, scale = 2) private BigDecimal valor;
    /** Subtotal minimo para o cupom valer. */
    @Column(precision = 12, scale = 2) private BigDecimal valorMinimo;
    /** Teto do desconto (util em cupons percentuais). */
    @Column(precision = 12, scale = 2) private BigDecimal valorMaximoDesconto;
    private LocalDateTime validoDe;
    private LocalDateTime validoAte;
    private boolean ativo = true;
    @Column(length = 200) private String descricao;
    private LocalDateTime criadoEm;
    @PrePersist void prePersist() { criadoEm = LocalDateTime.now(); }

    /** Motivo pelo qual o cupom nao vale para o subtotal informado, ou {@code null} se ele puder ser aplicado. */
    public String motivoInvalido(BigDecimal subtotal, LocalDateTime agora) {
        if (!ativo) return "Cupom inativo";
        if (validoDe != null && agora.isBefore(validoDe)) return "Cupom ainda nao esta valido";
        if (validoAte != null && agora.isAfter(validoAte)) return "Cupom expirado";
        if (valorMinimo != null && (subtotal == null || subtotal.compareTo(valorMinimo) < 0)) {
            return "Subtotal minimo para este cupom: R$ " + valorMinimo;
        }
        return null;
    }
}
