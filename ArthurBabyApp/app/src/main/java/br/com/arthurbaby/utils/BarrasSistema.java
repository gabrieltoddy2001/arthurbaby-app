package br.com.arthurbaby.utils;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import br.com.arthurbaby.R;

/**
 * Ajusta as telas ao modo "edge-to-edge", obrigatório a partir do Android 15 (targetSdk 35+).
 *
 * Sem isso o conteúdo era desenhado por baixo da barra de status: o cabeçalho ficava
 * sob o relógio e a seta de voltar caía numa área onde o sistema captura o toque.
 *
 * Chame {@link #aplicar(Activity)} logo após setContentView() em cada Activity.
 * Funciona em qualquer tamanho de aparelho, com recorte de câmera (notch) e teclado aberto.
 */
public final class BarrasSistema {

    private BarrasSistema() {}

    @SuppressWarnings("deprecation")
    public static void aplicar(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        if (Build.VERSION.SDK_INT < 35) {
            // Nas versões antigas a cor da barra é pintada pelo sistema; deixamos transparente
            // para aparecer a faixa desenhada abaixo (igual ao Android 15+).
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        View content = activity.findViewById(android.R.id.content);
        FaixasDrawable fundo = new FaixasDrawable(
                ContextCompat.getColor(activity, R.color.verde_arthur),
                ContextCompat.getColor(activity, R.color.branco));
        content.setBackground(fundo);

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            int baixo = Math.max(barras.bottom, teclado.bottom);

            v.setPadding(barras.left, barras.top, barras.right, baixo);
            fundo.setFaixas(barras.top, baixo);
            return WindowInsetsCompat.CONSUMED;
        });

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(false);     // ícones brancos sobre o verde
        controller.setAppearanceLightNavigationBars(true);  // ícones escuros sobre o branco

        ViewCompat.requestApplyInsets(content);
    }

    /** Pinta a faixa da barra de status (verde) e a da barra de navegação (branca). */
    private static class FaixasDrawable extends Drawable {
        private final Paint pTopo = new Paint();
        private final Paint pBase = new Paint();
        private int topo, base;

        FaixasDrawable(int corTopo, int corBase) {
            pTopo.setColor(corTopo);
            pBase.setColor(corBase);
        }

        void setFaixas(int topo, int base) {
            this.topo = topo;
            this.base = base;
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect b = getBounds();
            if (topo > 0) canvas.drawRect(b.left, b.top, b.right, b.top + topo, pTopo);
            if (base > 0) canvas.drawRect(b.left, b.bottom - base, b.right, b.bottom, pBase);
        }

        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(@Nullable ColorFilter cf) {}
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
