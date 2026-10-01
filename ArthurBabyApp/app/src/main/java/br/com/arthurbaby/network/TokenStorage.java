package br.com.arthurbaby.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {

    private static final String PREF_NAME = "arthurbaby_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOME = "nome";
    private static final String KEY_PERFIL = "perfil";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Salva os dados do login no "cofre" do celular.
     */
    public static void salvar(Context ctx, String token, Long usuarioId, String nome, String perfil) {
        prefs(ctx).edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USUARIO_ID, usuarioId != null ? usuarioId : -1)
                .putString(KEY_NOME, nome)
                .putString(KEY_PERFIL, perfil)
                .apply();
    }

    public static String getToken(Context ctx) {
        return prefs(ctx).getString(KEY_TOKEN, null);
    }

    public static Long getUsuarioId(Context ctx) {
        long id = prefs(ctx).getLong(KEY_USUARIO_ID, -1);
        return id == -1 ? null : id;
    }

    public static String getNome(Context ctx) {
        return prefs(ctx).getString(KEY_NOME, null);
    }

    public static String getPerfil(Context ctx) {
        return prefs(ctx).getString(KEY_PERFIL, null);
    }

    public static void limpar(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }
}