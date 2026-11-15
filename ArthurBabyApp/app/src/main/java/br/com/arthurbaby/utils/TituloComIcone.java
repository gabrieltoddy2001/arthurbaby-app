package br.com.arthurbaby.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import br.com.arthurbaby.R;

/**
 * Título de tela com um ícone logo depois do texto, dentro de um círculo suave.
 *
 * <pre>
 * &lt;br.com.arthurbaby.utils.TituloComIcone
 *     android:text="Meus Pedidos"
 *     app:iconeTitulo="@drawable/ic_pedidos" /&gt;
 * </pre>
 * Em cabeçalhos com degradê use app:iconeClaro="true" (ícone branco).
 */
public class TituloComIcone extends AppCompatTextView {

    private boolean claro;

    public TituloComIcone(Context context) { this(context, null); }

    public TituloComIcone(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMaxLines(1);
        setEllipsize(TextUtils.TruncateAt.END);
        setCompoundDrawablePadding(dp(10));

        int icone = 0;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TituloComIcone);
            icone = a.getResourceId(R.styleable.TituloComIcone_iconeTitulo, 0);
            claro = a.getBoolean(R.styleable.TituloComIcone_iconeClaro, false);
            a.recycle();
        }
        setIcone(icone);
    }

    /** Troca o ícone (0 remove). */
    public void setIcone(@DrawableRes int iconeRes) {
        if (iconeRes == 0) {
            setCompoundDrawablesRelative(null, null, null, null);
            return;
        }
        Drawable icone = ContextCompat.getDrawable(getContext(), iconeRes);
        if (icone == null) return;
        icone = DrawableCompat.wrap(icone.mutate());
        DrawableCompat.setTint(icone, claro
                ? ContextCompat.getColor(getContext(), R.color.branco)
                : ContextCompat.getColor(getContext(), R.color.rosa_arthur));

        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(claro
                ? 0x40FFFFFF
                : ContextCompat.getColor(getContext(), R.color.rosa_suave));

        int lado = dp(32), margem = dp(7);
        LayerDrawable composto = new LayerDrawable(new Drawable[]{circulo, icone});
        composto.setLayerInset(1, margem, margem, margem, margem);
        composto.setBounds(0, 0, lado, lado);
        setCompoundDrawablesRelative(null, null, composto, null);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
