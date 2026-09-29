package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.CupomValidacaoResponse;
import br.com.arthurbaby.entity.Cupom;
import br.com.arthurbaby.entity.Enums.TipoCupom;
import br.com.arthurbaby.repository.CupomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/** Regras de cupom compartilhadas pela validacao publica e pelo calculo do pedido. */
@Service
public class CupomService {
    private final CupomRepository cupons;

    public CupomService(CupomRepository cupons) {
        this.cupons = cupons;
    }

    /** Resultado da aplicacao de um cupom valido sobre um subtotal. */
    public record Aplicacao(Cupom cupom, BigDecimal desconto, boolean freteGratis) {}

    @Transactional(readOnly = true)
    public CupomValidacaoResponse validar(String codigo, BigDecimal subtotal) {
        String normalizado = normalizar(codigo);
        if (normalizado == null) return invalido(null, "Informe o codigo do cupom");
        if (subtotal == null || subtotal.signum() < 0) return invalido(normalizado, "Subtotal invalido");

        Optional<Cupom> encontrado = cupons.findByCodigoIgnoreCase(normalizado);
        if (encontrado.isEmpty()) return invalido(normalizado, "Cupom inexistente");
        Cupom cupom = encontrado.get();
        String motivo = cupom.motivoInvalido(subtotal, LocalDateTime.now());
        if (motivo != null) {
            return new CupomValidacaoResponse(false, cupom.getCodigo(), cupom.getTipo().name(), cupom.getDescricao(),
                    BigDecimal.ZERO, false, motivo);
        }
        Aplicacao aplicacao = calcular(cupom, subtotal);
        return new CupomValidacaoResponse(true, cupom.getCodigo(), cupom.getTipo().name(), cupom.getDescricao(),
                aplicacao.desconto(), aplicacao.freteGratis(), null);
    }

    /** Usado na criacao do pedido: cupom inexistente ou nao aplicavel gera 400. */
    public Aplicacao aplicar(String codigo, BigDecimal subtotal) {
        String normalizado = normalizar(codigo);
        Cupom cupom = cupons.findByCodigoIgnoreCase(normalizado)
                .orElseThrow(() -> new IllegalArgumentException("Cupom invalido"));
        String motivo = cupom.motivoInvalido(subtotal, LocalDateTime.now());
        if (motivo != null) throw new IllegalArgumentException("Cupom nao aplicavel: " + motivo);
        return calcular(cupom, subtotal);
    }

    static Aplicacao calcular(Cupom cupom, BigDecimal subtotal) {
        BigDecimal valor = cupom.getValor() == null ? BigDecimal.ZERO : cupom.getValor();
        BigDecimal desconto = switch (cupom.getTipo()) {
            case PERCENTUAL -> subtotal.multiply(valor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case VALOR_FIXO -> valor.setScale(2, RoundingMode.HALF_UP);
            case FRETE_GRATIS -> BigDecimal.ZERO.setScale(2);
        };
        if (cupom.getValorMaximoDesconto() != null && desconto.compareTo(cupom.getValorMaximoDesconto()) > 0) {
            desconto = cupom.getValorMaximoDesconto();
        }
        if (desconto.compareTo(subtotal) > 0) desconto = subtotal;
        return new Aplicacao(cupom, desconto, cupom.getTipo() == TipoCupom.FRETE_GRATIS);
    }

    private static String normalizar(String codigo) {
        return codigo == null || codigo.isBlank() ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }

    private static CupomValidacaoResponse invalido(String codigo, String motivo) {
        return new CupomValidacaoResponse(false, codigo, null, null, BigDecimal.ZERO, false, motivo);
    }
}
