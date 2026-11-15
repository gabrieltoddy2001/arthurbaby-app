package br.com.arthurbaby.utils;

/**
 * Política de senha da loja (a mesma da API, PoliticaSenha.java do backend):
 * de 8 a 72 caracteres, com pelo menos uma letra e um número.
 * Usada no cadastro, na troca e na redefinição de senha. O login não aplica a política,
 * para contas antigas continuarem entrando. A API valida de novo; aqui é só para avisar antes de enviar.
 */
public final class PoliticaSenha {
    public static final String DESCRICAO = "Use de 8 a 72 caracteres, com pelo menos uma letra e um número.";

    private PoliticaSenha() {}

    /** Mensagem de erro, ou null se a senha estiver de acordo com a política. */
    public static String erro(String senha) {
        if (senha == null || senha.length() < 8 || senha.length() > 72) return DESCRICAO;
        boolean temLetra = false, temNumero = false;
        for (int i = 0; i < senha.length(); i++) {
            char c = senha.charAt(i);
            if (Character.isLetter(c)) temLetra = true;
            else if (Character.isDigit(c)) temNumero = true;
        }
        return temLetra && temNumero ? null : DESCRICAO;
    }
}
