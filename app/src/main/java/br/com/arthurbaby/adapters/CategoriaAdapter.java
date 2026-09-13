package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

        // Seta do lado direito — indica que tem subcategorias
        h.tvSeta.setVisibility(c.temSubcategorias() ? View.VISIBLE : View.VISIBLE);
        h.tvSeta.setText(c.temSubcategorias() ? "›+ " + c.getSubcategorias().size() : "›");

        // Container de subcategorias (chips em linha)
        h.containerSub.removeAllViews();
        if (c.temSubcategorias()) {
            for (Categoria sub : c.getSubcategorias()) {
                TextView chip = new TextView(h.itemView.getContext());
                chip.setText(sub.getNome());
                chip.setTextSize(12f);
                chip.setPadding(28, 10, 28, 10);
                chip.setBackgroundColor(0xFFF0F2F5);
                chip.setTextColor(0xFF1E293B);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 10, 0);
                chip.setLayoutParams(lp);

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
    public int getItemCount() {
        return categorias.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icone;
        TextView tv, tvSeta;
        LinearLayout containerSub;

        VH(View v) {
            super(v);
            icone = v.findViewById(R.id.imgCategoria);
            tv = v.findViewById(R.id.tvCategoria);
            tvSeta = v.findViewById(R.id.tvSeta);
            containerSub = v.findViewById(R.id.containerSub);
        }
    }
}