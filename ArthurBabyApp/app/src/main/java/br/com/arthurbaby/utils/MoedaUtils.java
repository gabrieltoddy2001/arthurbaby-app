package br.com.arthurbaby.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoedaUtils {

    /**
     * Formata um BigDecimal como moeda brasileira (R$ 129,90).
     */
    public static String formatar(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor);
    }

    /**
     * Formata um double como moeda brasileira.
     */
    public static String formatar(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(valor);
    }

    /**
     * Retorna o formatador para uso manual (raro).
     */
    public static NumberFormat getFormatador() {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    }
}