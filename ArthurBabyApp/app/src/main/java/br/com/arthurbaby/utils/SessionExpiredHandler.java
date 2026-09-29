package br.com.arthurbaby.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import br.com.arthurbaby.activities.LoginActivity;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class SessionExpiredHandler {

    private static boolean jaAvisado = false;

    /**
     * Trata uma resposta 401 (token expirado).
     * Limpa o token, avisa o usuário e redireciona para o Login.
     */
    public static void tratar(Context context) {
        // Evita múltiplos redirecionamentos simultâneos
        if (jaAvisado) return;
        jaAvisado = true;

        // Limpa tudo
        TokenStorage.limpar(context);
        FavoritoRepository.getInstance().limpar();

        // Avisa + redireciona na thread principal
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context,
                    "Sua sessão expirou. Faça login novamente.",
                    Toast.LENGTH_LONG).show();

            Intent i = new Intent(context, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        });

        // Libera o flag após 5 segundos (caso o usuário volte a logar)
        new Handler(Looper.getMainLooper()).postDelayed(() -> jaAvisado = false, 5000);
    }
}