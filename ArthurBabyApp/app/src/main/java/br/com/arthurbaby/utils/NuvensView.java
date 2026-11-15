package br.com.arthurbaby.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import br.com.arthurbaby.R;

/**
 * Fundo da tela de login: faixa superior com o degradê verde → rosa do app, fundo creme
 * e nuvens infantis bem suaves (rosa/verde pastel) passando devagar na parte clara,
 * por trás do cartão de login.
 * Respeita "remover animações" da acessibilidade (nuvens ficam paradas).
 */
public class NuvensView extends View {

    /** Altura (dp) da faixa com degradê no topo — igual ao cabeçalho do login. */
    private static final float ALTURA_FAIXA_DP = 260f;

    /**
     * Nuvens só na parte clara (abaixo do degradê), passando por trás do cartão de login.
     * Cada uma: distância (dp) abaixo do fim do degradê, largura (dp), velocidade (dp/s),
     * posição inicial (fração da largura da tela) e cor (0 = rosa, 1 = verde).
     */
    private static final float[][] NUVENS = {
            // y,   larg, veloc, x0,   cor
            {18,    92,  7f,   0.70f, 0},
            {120,  118,  5f,   0.08f, 1},
            {240,   84,  8.5f, 0.45f, 0},
            {350,  106,  6f,   0.88f, 1},
            {460,   96,  7.5f, 0.25f, 0},
            {565,  122,  5.5f, 0.60f, 1},
    };

    private final Paint paintFaixa = new Paint();
    private final Paint paintFundo = new Paint();
    private final Paint[] paintsNuvem = new Paint[2];
    private final Path nuvem = new Path();
    private final RectF base = new RectF();
    private final float densidade;

    private float alturaFaixa;   // px da faixa com degradê
    private float segundos;      // tempo de animação acumulado
    private ValueAnimator animador;

    public NuvensView(Context context) { this(context, null); }

    public NuvensView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        densidade = getResources().getDisplayMetrics().density;
        paintFundo.setColor(ContextCompat.getColor(context, R.color.branco_quente));
        paintsNuvem[0] = pintura(comAlfa(ContextCompat.getColor(context, R.color.rosa_claro), 0.45f));
        paintsNuvem[1] = pintura(comAlfa(ContextCompat.getColor(context, R.color.verde_claro), 0.40f));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    private static Paint pintura(int cor) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(cor);
        return p;
    }

    private static int comAlfa(int cor, float alfa) {
        return (Math.round(alfa * 255) << 24) | (cor & 0x00FFFFFF);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        alturaFaixa = ALTURA_FAIXA_DP * densidade;
        paintFaixa.setShader(new LinearGradient(0, 0, w, 0,
                ContextCompat.getColor(getContext(), R.color.verde_arthur),
                ContextCompat.getColor(getContext(), R.color.rosa_arthur),
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int w = getWidth(), h = getHeight();
        canvas.drawRect(0, alturaFaixa, w, h, paintFundo);
        canvas.drawRect(0, 0, w, Math.min(alturaFaixa, h), paintFaixa);

        for (float[] n : NUVENS) {
            float y = alturaFaixa + n[0] * densidade; // só na parte clara
            if (y > h) continue;
            float largura = n[1] * densidade;
            // percorre da esquerda para a direita e recomeça fora da tela à esquerda
            float percurso = w + largura;
            float x = ((n[3] * percurso + n[2] * densidade * segundos) % percurso) - largura;
            desenharNuvem(canvas, x, y, largura, paintsNuvem[(int) n[4]]);
        }
    }

    /** Nuvem estilo infantil: base arredondada + três "bolas" no topo. */
    private void desenharNuvem(Canvas c, float x, float y, float largura, Paint p) {
        float altura = largura * 0.42f;
        float baseTopo = y + altura * 0.45f;
        nuvem.reset();
        base.set(x, baseTopo, x + largura, y + altura);
        nuvem.addRoundRect(base, altura * 0.28f, altura * 0.28f, Path.Direction.CW);
        nuvem.addCircle(x + largura * 0.30f, baseTopo, altura * 0.36f, Path.Direction.CW);
        nuvem.addCircle(x + largura * 0.56f, y + altura * 0.30f, altura * 0.46f, Path.Direction.CW);
        nuvem.addCircle(x + largura * 0.78f, baseTopo + altura * 0.04f, altura * 0.30f, Path.Direction.CW);
        // união das formas (evita sobreposição mais escura nas partes translúcidas)
        nuvem.setFillType(Path.FillType.WINDING);
        c.drawPath(nuvem, p);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) return;
        animador = ValueAnimator.ofFloat(0f, 1f);
        animador.setDuration(1000);
        animador.setRepeatCount(ValueAnimator.INFINITE);
        animador.setInterpolator(new LinearInterpolator());
        final long inicio = System.nanoTime();
        animador.addUpdateListener(a -> {
            segundos = (System.nanoTime() - inicio) / 1_000_000_000f;
            invalidate();
        });
        animador.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animador != null) animador.cancel();
        animador = null;
        super.onDetachedFromWindow();
    }
}
