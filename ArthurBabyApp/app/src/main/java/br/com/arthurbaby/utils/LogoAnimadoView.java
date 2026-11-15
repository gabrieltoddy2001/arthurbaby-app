package br.com.arthurbaby.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.com.arthurbaby.R;

/**
 * Logo circular da ArthurBaby com animação:
 *  - 3 estrelinhas que brilham no canto superior esquerdo (herdadas de {@link EstrelinhasView});
 *  - um reflexo de luz que atravessa a logo de tempos em tempos.
 *
 * Basta usar no XML com o tamanho desejado; tudo se ajusta proporcionalmente:
 *   <br.com.arthurbaby.utils.LogoAnimadoView android:layout_width="120dp" android:layout_height="120dp" />
 */
public class LogoAnimadoView extends EstrelinhasView {

    /** Margem em volta da logo (fração do lado) que abre espaço para as estrelas no canto. */
    private static final float MARGEM_LOGO = 0.08f;
    private static final long CICLO_REFLEXO = 4200;
    private static final float FRACAO_REFLEXO = 0.30f; // parte do ciclo em que o reflexo passa

    private final ImageView imgLogo;
    private final Paint paintReflexo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path circulo = new Path();
    private final Matrix matrizReflexo = new Matrix();
    private LinearGradient gradiente;
    private float progressoReflexo = -1f;
    private ValueAnimator animReflexo;

    public LogoAnimadoView(@NonNull Context context) { this(context, null); }

    public LogoAnimadoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        imgLogo = new ImageView(context);
        imgLogo.setImageResource(R.drawable.logo_circular);
        imgLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgLogo.setContentDescription("Logo ArthurBaby");
        addView(imgLogo, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int lado = Math.min(w, h);
        int margem = Math.round(lado * MARGEM_LOGO);
        imgLogo.setPadding(margem, margem, margem, margem);

        // Reflexo: faixa diagonal branca, recortada no círculo interno da logo
        float r = lado / 2f;
        circulo.reset();
        circulo.addCircle(w / 2f, h / 2f, r * (1f - 2 * MARGEM_LOGO) * 0.965f, Path.Direction.CW);
        float faixa = lado * 0.22f;
        gradiente = new LinearGradient(-faixa, 0, faixa, 0,
                new int[]{0x00FFFFFF, 0x99FFFFFF, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        paintReflexo.setShader(gradiente);
    }

    @Override
    protected void desenharEfeito(@NonNull Canvas canvas) {
        if (progressoReflexo < 0f || gradiente == null) return;
        int lado = Math.min(getWidth(), getHeight());
        float x = -lado * 0.4f + progressoReflexo * lado * 1.8f;
        matrizReflexo.reset();
        matrizReflexo.setRotate(25f);
        matrizReflexo.postTranslate(x, 0);
        gradiente.setLocalMatrix(matrizReflexo);
        canvas.save();
        canvas.clipPath(circulo);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paintReflexo);
        canvas.restore();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        pararReflexo();
        if (animacoesDesligadas()) return;
        animReflexo = ValueAnimator.ofFloat(0f, 1f);
        animReflexo.setDuration(CICLO_REFLEXO);
        animReflexo.setStartDelay(600);
        animReflexo.setRepeatCount(ValueAnimator.INFINITE);
        animReflexo.setInterpolator(new LinearInterpolator());
        animReflexo.addUpdateListener(an -> {
            float t = (float) an.getAnimatedValue();
            progressoReflexo = t < FRACAO_REFLEXO ? t / FRACAO_REFLEXO : -1f;
            invalidate();
        });
        animReflexo.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        pararReflexo();
        super.onDetachedFromWindow();
    }

    private void pararReflexo() {
        if (animReflexo != null) animReflexo.cancel();
        animReflexo = null;
        progressoReflexo = -1f;
    }
}
