package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/** Regra de frete aplicada pelo servidor: retirada na loja e gratis; entrega e gratis a partir de um subtotal minimo. */
@Service
public class FreteService {
    private final BigDecimal valorEntrega;
    private final BigDecimal gratisAPartirDe;

    public FreteService(@Value("${app.frete.valor-entrega:15.00}") BigDecimal valorEntrega,
                        @Value("${app.frete.gratis-a-partir-de:200.00}") BigDecimal gratisAPartirDe) {
        this.valorEntrega = valorEntrega;
        this.gratisAPartirDe = gratisAPartirDe;
    }

    public BigDecimal calcular(FormaRecebimento forma, BigDecimal subtotal) {
        if (forma != FormaRecebimento.ENTREGA) return BigDecimal.ZERO;
        if (subtotal != null && subtotal.compareTo(gratisAPartirDe) >= 0) return BigDecimal.ZERO;
        return valorEntrega;
    }
}
