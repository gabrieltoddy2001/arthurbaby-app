package br.com.arthurbaby.utils;

import java.util.Locale;

/** Formata a média de avaliações (0 a 5) vinda do back. */
public final class AvaliacaoUtils {

    private AvaliacaoUtils() {}

    /**
     * Ex.: 4.5 → "★★★★☆", 4.6 → "★★★★★", 3.2 → "★★★☆☆", 0 → "☆☆☆☆☆".
     * Meia estrela arredonda para baixo, para a nota 4,5 não aparecer como 5 estrelas cheias.
     */
    public static String estrelas(double media) {
        int cheias = (int) Math.ceil(Math.max(0, Math.min(5, media)) - 0.5);
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
