package br.com.arthurbaby.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import br.com.arthurbaby.R;

public class NavUtils {

    /**
     * Troca o fragment com animação.
     */
    public static void trocar(FragmentActivity activity, Fragment novo) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out)
                .replace(R.id.frameContainer, novo)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Volta para a tela anterior com animação.
     */
    public static void voltar(FragmentActivity activity) {
        activity.getSupportFragmentManager().popBackStack();
    }

    /**
     * Substitui sem empilhar (usado pelo BottomNav).
     */
    public static void trocarSemEmpilhar(FragmentActivity activity, Fragment novo) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out)
                .replace(R.id.frameContainer, novo)
                .commit();
    }
}