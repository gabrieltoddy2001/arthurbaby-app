package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Categoria;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.VH> {

    public interface OnClick {
        void onClick(Categoria categoria);
    }

    private final List<Categoria> categorias;
    private final OnClick listener;

    public CategoriaAdapter(List<Categoria> categorias, OnClick listener) {
        this.categorias = categorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Categoria c = categorias.get(position);
        h.tv.setText(c.getNome());

        if (c.getIconeRes() != 0) {
            h.icone.setImageResource(c.getIconeRes());
        }

        // Contador de subcategorias
        if (c.temSubcategorias()) {
            h.tvSeta.setText("› " + c.getSubcategorias().size());
        } else {
            h.tvSeta.setText("›");
        }

        // Container de subcategorias (chips em linha)
        h.containerSub.removeAllViews();
        if (c.temSubcategorias()) {
            for (Categoria sub : c.getSubcategorias()) {
                float dp = h.itemView.getResources().getDisplayMetrics().density;
                TextView chip = new TextView(h.itemView.getContext());
                chip.setText(sub.getNome());
                chip.setTextSize(12f);
                chip.setMaxLines(1);
                chip.setPadding((int) (12 * dp), (int) (5 * dp), (int) (12 * dp), (int) (5 * dp));
                android.graphics.drawable.GradientDrawable fundo =
                        new android.graphics.drawable.GradientDrawable();
                fundo.setColor(0xFFFAD1DE);
                fundo.setCornerRadius(14 * dp);
                chip.setBackground(fundo);
                chip.setTextColor(0xFFD56586);
                chip.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                chip.setOnClickListener(v -> listener.onClick(sub));
                h.containerSub.addView(chip);
            }
            h.containerSub.setVisibility(View.VISIBLE);
        } else {
            h.containerSub.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() { return categorias.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icone;
        TextView tv, tvSeta;
        ViewGroup containerSub;

        VH(View v) {
            super(v);
            icone = v.findViewById(R.id.imgCategoria);
            tv = v.findViewById(R.id.tvCategoria);
            tvSeta = v.findViewById(R.id.tvSeta);
            containerSub = v.findViewById(R.id.containerSub);
        }
    }
}