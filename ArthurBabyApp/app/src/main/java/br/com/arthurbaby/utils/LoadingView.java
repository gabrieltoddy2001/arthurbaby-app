package br.com.arthurbaby.utils;

import android.view.View;

public class LoadingView {

    /**
     * Mostra o ProgressBar e esconde a lista.
     */
    public static void mostrar(View progressBar, View... conteudos) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        for (View v : conteudos) {
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    /**
     * Esconde o ProgressBar e mostra a lista.
     */
    public static void esconder(View progressBar, View... conteudos) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        for (View v : conteudos) {
            if (v != null) v.setVisibility(View.VISIBLE);
        }
    }
}