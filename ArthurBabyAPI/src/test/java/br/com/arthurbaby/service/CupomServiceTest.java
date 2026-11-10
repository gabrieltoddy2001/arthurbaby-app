package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.CupomResponse;
import br.com.arthurbaby.dto.CupomValidacaoResponse;
import br.com.arthurbaby.entity.Cupom;
import br.com.arthurbaby.entity.Enums.TipoCupom;
import br.com.arthurbaby.repository.CupomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CupomServiceTest {
    private CupomRepository cupons;
    private CupomService service;

    @BeforeEach
    void setUp() {
        cupons = mock(CupomRepository.class);
        service = new CupomService(cupons);
    }

    @Test
    void listarAtivosMapeiaCuponsVigentesDoRepositorio() {
        Cupom percentual = cupom("ARTHUR10", TipoCupom.PERCENTUAL, "10.00");
        percentual.setDescricao("10% de desconto em todo o site");
        percentual.setValidoAte(LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        Cupom frete = cupom("FRETEGRATIS", TipoCupom.FRETE_GRATIS, null);
        when(cupons.findCuponsAtivos(any(LocalDateTime.class))).thenReturn(List.of(percentual, frete));

        List<CupomResponse> ativos = service.listarAtivos();

        assertEquals(2, ativos.size());
        assertEquals("ARTHUR10", ativos.get(0).codigo());
        assertEquals("PERCENTUAL", ativos.get(0).tipo());
        assertEquals(new BigDecimal("10.00"), ativos.get(0).valor());
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59), ativos.get(0).validoAte());
        // FRETE_GRATIS sem valor e sem data limite: valor 0 e validoAte nulo
        assertEquals(BigDecimal.ZERO, ativos.get(1).valor());
        assertNull(ativos.get(1).validoAte());
    }

    @Test
    void validarCupomPercentualCalculaDescontoSobreSubtotal() {
        when(cupons.findByCodigoIgnoreCase("ARTHUR10")).thenReturn(Optional.of(cupom("ARTHUR10", TipoCupom.PERCENTUAL, "10.00")));

        CupomValidacaoResponse r = service.validar(" arthur10 ", new BigDecimal("149.90"));

        assertTrue(r.valido());
        assertEquals("ARTHUR10", r.codigo());
        assertEquals("PERCENTUAL", r.tipo());
        assertEquals(new BigDecimal("14.99"), r.desconto());
        assertFalse(r.freteGratis());
        assertNull(r.motivo());
    }

    @Test
    void validarFreteGratisEValorFixoLimitadoAoSubtotal() {
        when(cupons.findByCodigoIgnoreCase("FRETEGRATIS")).thenReturn(Optional.of(cupom("FRETEGRATIS", TipoCupom.FRETE_GRATIS, null)));
        when(cupons.findByCodigoIgnoreCase("BEMVINDO")).thenReturn(Optional.of(cupom("BEMVINDO", TipoCupom.VALOR_FIXO, "15.00")));

        CupomValidacaoResponse frete = service.validar("FRETEGRATIS", new BigDecimal("50.00"));
        assertTrue(frete.valido());
        assertTrue(frete.freteGratis());
        assertEquals(0, frete.desconto().signum());

        assertEquals(new BigDecimal("15.00"), service.validar("BEMVINDO", new BigDecimal("100.00")).desconto());
        assertEquals(new BigDecimal("9.90"), service.validar("BEMVINDO", new BigDecimal("9.90")).desconto());
    }

    @Test
    void validarRespeitaTetoDeDesconto() {
        Cupom cupom = cupom("METADE", TipoCupom.PERCENTUAL, "50.00");
        cupom.setValorMaximoDesconto(new BigDecimal("30.00"));
        when(cupons.findByCodigoIgnoreCase("METADE")).thenReturn(Optional.of(cupom));

        assertEquals(new BigDecimal("30.00"), service.validar("METADE", new BigDecimal("200.00")).desconto());
    }

    @Test
    void validarDevolveMotivoQuandoCupomNaoVale() {
        when(cupons.findByCodigoIgnoreCase("NADA")).thenReturn(Optional.empty());
        Cupom inativo = cupom("INATIVO", TipoCupom.PERCENTUAL, "10");
        inativo.setAtivo(false);
        when(cupons.findByCodigoIgnoreCase("INATIVO")).thenReturn(Optional.of(inativo));
        Cupom expirado = cupom("EXPIRADO", TipoCupom.PERCENTUAL, "10");
        expirado.setValidoAte(LocalDateTime.now().minusDays(1));
        when(cupons.findByCodigoIgnoreCase("EXPIRADO")).thenReturn(Optional.of(expirado));
        Cupom futuro = cupom("FUTURO", TipoCupom.PERCENTUAL, "10");
        futuro.setValidoDe(LocalDateTime.now().plusDays(1));
        when(cupons.findByCodigoIgnoreCase("FUTURO")).thenReturn(Optional.of(futuro));
        Cupom minimo = cupom("MINIMO", TipoCupom.VALOR_FIXO, "20");
        minimo.setValorMinimo(new BigDecimal("100.00"));
        when(cupons.findByCodigoIgnoreCase("MINIMO")).thenReturn(Optional.of(minimo));

        for (String codigo : new String[]{"NADA", "INATIVO", "EXPIRADO", "FUTURO", "MINIMO"}) {
            CupomValidacaoResponse r = service.validar(codigo, new BigDecimal("99.99"));
            assertFalse(r.valido(), codigo);
            assertEquals(0, r.desconto().signum(), codigo);
            assertTrue(r.motivo() != null && !r.motivo().isBlank(), codigo);
        }
        assertFalse(service.validar(" ", new BigDecimal("10")).valido());
    }

    @Test
    void aplicarLancaExcecaoParaCupomInvalido() {
        when(cupons.findByCodigoIgnoreCase("HACKEADO")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.aplicar("HACKEADO", new BigDecimal("10.00")));
    }

    private static Cupom cupom(String codigo, TipoCupom tipo, String valor) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setTipo(tipo);
        cupom.setValor(valor == null ? null : new BigDecimal(valor));
        return cupom;
    }
}
