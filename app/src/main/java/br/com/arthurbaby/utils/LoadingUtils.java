package br.com.arthurbaby.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import br.com.arthurbaby.R;

public class LoadingUtils {

    private static AlertDialog dialog;

    public static void mostrar(Context ctx) {
        if (dialog != null && dialog.isShowing()) return;

        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_loading, null);

        dialog = new AlertDialog.Builder(ctx)
                .setView(view)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
    }

    public static void esconder() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}