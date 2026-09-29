package br.com.arthurbaby.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import br.com.arthurbaby.activities.LoginActivity;
import br.com.arthurbaby.network.TokenStorage;

public class AuthGuard {

    /**
     * Verifica se o usuário está logado.
     */
    public static boolean estaLogado(android.content.Context ctx) {
        String token = TokenStorage.getToken(ctx);
        return token != null && !token.isEmpty();
    }

    /**
     * Executa uma ação se o usuário estiver logado.
     * Se não estiver, mostra um dialog amigável pedindo login.
     */
    public static void executarSeLogado(Activity activity, Runnable acao) {
        if (estaLogado(activity)) {
            acao.run();
        } else {
            mostrarDialogLogin(activity, null);
        }
    }

    /**
     * Mostra o dialog "Faça login para continuar".
     */
    public static void mostrarDialogLogin(Activity activity, String mensagem) {
        String texto = (mensagem != null && !mensagem.isEmpty())
                ? mensagem
                : "Você precisa entrar na sua conta para continuar.";

        new AlertDialog.Builder(activity)
                .setTitle("Faça login para continuar")
                .setMessage(texto)
                .setPositiveButton("Entrar", (d, w) -> {
                    Intent i = new Intent(activity, LoginActivity.class);
                    activity.startActivity(i);
                })
                .setNegativeButton("Agora não", null)
                .show();
    }
}