package br.com.arthurbaby.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.R;

/**
 * Container que desenha 3 estrelinhas brilhando (piscam e giram levemente) no canto
 * superior esquerdo, por cima do conteúdo. É o mesmo efeito da logo.
 *
 * Coloque o conteúdo (ex.: o círculo do avatar) dentro, centralizado e um pouco menor
 * que o container (~8% de margem), para as estrelas ficarem no canto sem cobri-lo:
 * <pre>
 * &lt;br.com.arthurbaby.utils.EstrelinhasView android:layout_width="116dp" android:layout_height="116dp"&gt;
 *     &lt;FrameLayout android:layout_width="100dp" android:layout_height="100dp"
 *         android:layout_gravity="center" ... /&gt;
 * &lt;/br.com.arthurbaby.utils.EstrelinhasView&gt;
 * </pre>
 */
public class EstrelinhasView extends FrameLayout {

    /** Estrela: tamanho e posição (x, y) em fração do lado da view, e atraso da animação. */
    private static final float[][] ESTRELAS = {
            // tamanho, x,    y,    atraso(ms)  — todas no canto, fora do círculo central
            {0.20f, 0.01f, 0.01f, 0},     // grande
            {0.09f, 0.26f, 0.00f, 550},   // pequena, à direita
            {0.11f, 0.00f, 0.25f, 1100},  // média, abaixo
    };
    private static final long DURACAO_BRILHO = 1800;

    private final List<ImageView> estrelas = new ArrayList<>();
    private final List<ValueAnimator> animadores = new ArrayList<>();

    public EstrelinhasView(@NonNull Context context) { this(context, null); }

    public EstrelinhasView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setClipChildren(false);
        for (int i = 0; i < ESTRELAS.length; i++) {
            ImageView e = new ImageView(context);
            e.setImageResource(R.drawable.ic_brilho);
            e.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            e.setAlpha(0f);
            estrelas.add(e);
            addView(e, new LayoutParams(0, 0));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int lado = Math.min(w, h);
        for (int i = 0; i < estrelas.size(); i++) {
            float[] cfg = ESTRELAS[i];
            ImageView e = estrelas.get(i);
            LayoutParams lp = (LayoutParams) e.getLayoutParams();
            lp.width = lp.height = Math.round(lado * cfg[0]);
            lp.leftMargin = Math.round(lado * cfg[1]);
            lp.topMargin = Math.round(lado * cfg[2]);
            e.setLayoutParams(lp);
        }
        // As estrelas mudaram de tamanho durante o layout: agenda uma nova passada de layout
        post(this::requestLayout);
    }

    /** Desenha entre o conteúdo e as estrelas (a logo usa para o reflexo de luz). */
    protected void desenharEfeito(@NonNull Canvas canvas) {}

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        // Conteúdo primeiro, depois o efeito extra, e as estrelas sempre por cima
        long t = getDrawingTime();
        for (int i = 0; i < getChildCount(); i++) {
            View filho = getChildAt(i);
            if (!estrelas.contains(filho) && filho.getVisibility() == VISIBLE) {
                drawChild(canvas, filho, t);
            }
        }
        desenharEfeito(canvas);
        for (ImageView e : estrelas) drawChild(canvas, e, t);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        iniciarBrilho();
    }

    @Override
    protected void onDetachedFromWindow() {
        pararBrilho();
        super.onDetachedFromWindow();
    }

    /** true se o aparelho está com animações desligadas (acessibilidade). */
    protected static boolean animacoesDesligadas() {
        return Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled();
    }

    private void iniciarBrilho() {
        pararBrilho();
        if (animacoesDesligadas()) {
            for (ImageView e : estrelas) { e.setAlpha(1f); e.setScaleX(0.9f); e.setScaleY(0.9f); }
            return;
        }
        for (int i = 0; i < estrelas.size(); i++) {
            ImageView e = estrelas.get(i);
            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(DURACAO_BRILHO);
            a.setStartDelay((long) ESTRELAS[i][3]);
            a.setRepeatCount(ValueAnimator.INFINITE);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(an -> {
                float p = (float) an.getAnimatedValue();
                // sobe e desce (0 → 1 → 0)
                float pulso = (float) Math.sin(Math.PI * p);
                e.setAlpha(0.15f + 0.85f * pulso);
                float escala = 0.35f + 0.75f * pulso;
                e.setScaleX(escala);
                e.setScaleY(escala);
                e.setRotation(-20f + 40f * p);
            });
            a.start();
            animadores.add(a);
        }
    }

    private void pararBrilho() {
        for (ValueAnimator a : animadores) a.cancel();
        animadores.clear();
    }
}
