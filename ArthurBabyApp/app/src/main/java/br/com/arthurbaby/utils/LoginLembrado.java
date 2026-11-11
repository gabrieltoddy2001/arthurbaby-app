package br.com.arthurbaby.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda o último e-mail/CPF usado no login para preencher a tela ao entrar novamente.
 *
 * Fica num arquivo separado da sessão (TokenStorage), por isso continua salvo após o logout.
 * Guarda apenas o e-mail/CPF, nunca a senha.
 */
public final class LoginLembrado {

    private static final String PREF_NAME = "arthurbaby_login_lembrado";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_LEMBRAR = "lembrar";

    private LoginLembrado() {}

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Último e-mail/CPF salvo, ou "" se não houver (ou se a pessoa desmarcou "lembrar"). */
    public static String getLogin(Context ctx) {
        return isLembrar(ctx) ? prefs(ctx).getString(KEY_LOGIN, "") : "";
    }

    /** Preferência "lembrar" (marcada por padrão). */
    public static boolean isLembrar(Context ctx) {
        return prefs(ctx).getBoolean(KEY_LEMBRAR, true);
    }

    /** Chamado após um login bem-sucedido. */
    public static void salvar(Context ctx, String login, boolean lembrar) {
        SharedPreferences.Editor e = prefs(ctx).edit().putBoolean(KEY_LEMBRAR, lembrar);
        if (lembrar) e.putString(KEY_LOGIN, login);
        else e.remove(KEY_LOGIN);
        e.apply();
    }
}
