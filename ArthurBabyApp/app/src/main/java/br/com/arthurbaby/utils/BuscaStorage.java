package br.com.arthurbaby.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BuscaStorage {

    private static final String PREF_NAME = "arthurbaby_buscas";
    private static final String KEY_HISTORICO = "historico";
    private static final int MAX_ITENS = 10;

    /**
     * Adiciona uma busca ao histórico (no topo).
     * Remove duplicatas e limita a MAX_ITENS.
     */
    public static void adicionar(Context ctx, String termo) {
        if (termo == null) return;
        termo = termo.trim();
        if (termo.length() < 2) return; // ignora buscas muito curtas

        List<String> atual = getHistorico(ctx);

        // Remove se já existe (para ir pro topo)
        atual.remove(termo);

        // Adiciona no início
        atual.add(0, termo);

        // Limita
        while (atual.size() > MAX_ITENS) {
            atual.remove(atual.size() - 1);
        }

        salvar(ctx, atual);
    }

    /**
     * Lê o histórico salvo.
     */
    public static List<String> getHistorico(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORICO, null);

        if (json == null || json.isEmpty()) return new ArrayList<>();

        try {
            Type tipo = new TypeToken<List<String>>(){}.getType();
            List<String> lista = new Gson().fromJson(json, tipo);
            return lista != null ? lista : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Limpa o histórico.
     */
    public static void limpar(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private static void salvar(Context ctx, List<String> lista) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORICO, new Gson().toJson(lista)).apply();
    }
}