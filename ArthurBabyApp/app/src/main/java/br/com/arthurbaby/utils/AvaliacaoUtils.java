package br.com.arthurbaby.utils;

import java.util.Locale;

/** Formata a média de avaliações (0 a 5) vinda do back. */
public final class AvaliacaoUtils {

    private AvaliacaoUtils() {}

    /** Ex.: 4.5 → "★★★★★", 3.2 → "★★★☆☆", 0 → "☆☆☆☆☆" (arredonda para a estrela mais próxima). */
    public static String estrelas(double media) {
        int cheias = (int) Math.round(Math.max(0, Math.min(5, media)));
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) sb.append(i < cheias ? '★' : '☆');
        return sb.toString();
    }

    /** Ex.: 4.5 → "(4,5)"; sem avaliações → texto informado. */
    public static String nota(double media, String semAvaliacao) {
        if (media <= 0) return semAvaliacao;
        return String.format(new Locale("pt", "BR"), "(%.1f)", media);
    }
}
