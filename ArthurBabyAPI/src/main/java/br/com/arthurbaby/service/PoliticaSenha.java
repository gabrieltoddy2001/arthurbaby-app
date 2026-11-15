package br.com.arthurbaby.service;

import java.util.Locale;

/**
 * Política de senha única da loja, aplicada sempre que uma senha é definida: cadastro no app, troca de senha,
 * recuperação por token e redefinição pelo painel. (O login não aplica a política: contas antigas continuam entrando.)
 *
 * <ul>
 *   <li>de 8 a 72 caracteres (72 é o limite do BCrypt);</li>
 *   <li>pelo menos uma letra e um número;</li>
 *   <li>não pode ser igual ao e-mail nem à parte antes do "@" (CPF puro já é barrado pela regra da letra).</li>
 * </ul>
 */
public final class PoliticaSenha {
    public static final int MINIMO = 8;
    public static final int MAXIMO = 72;
    public static final String DESCRICAO =
            "A senha deve ter de " + MINIMO + " a " + MAXIMO + " caracteres, com pelo menos uma letra e um número.";

    private PoliticaSenha() {}

    public static void validar(String senha) {
        validar(senha, null, null);
    }

    /** Lança IllegalArgumentException (400) com a mensagem exibida ao usuário. */
    public static void validar(String senha, String email, String cpf) {
        if (senha == null || senha.length() < MINIMO || senha.length() > MAXIMO
                || !senha.matches(".*\\p{L}.*") || !senha.matches(".*\\d.*")) {
            throw new IllegalArgumentException(DESCRICAO);
        }
        String s = senha.toLowerCase(Locale.ROOT);
        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase(Locale.ROOT);
            String usuario = e.contains("@") ? e.substring(0, e.indexOf('@')) : e;
            if (s.equals(e) || (!usuario.isEmpty() && s.equals(usuario))) {
                throw new IllegalArgumentException("A senha não pode ser igual ao seu e-mail.");
            }
        }
        // CPF: uma senha só com os dígitos do CPF já é recusada por não ter letra
    }
}
