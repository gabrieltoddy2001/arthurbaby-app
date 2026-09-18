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

public class CategoriaIconeAdapter extends RecyclerView.Adapter<CategoriaIconeAdapter.VH> {

    public interface OnClick {
        void onClick(Categoria c);
    }

    private final List<Categoria> categorias;
    private final OnClick listener;

    public CategoriaIconeAdapter(List<Categoria> categorias, OnClick listener) {
        this.categorias = categorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria_icone, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Categoria c = categorias.get(position);
        h.tvNome.setText(c.getNome());
        if (c.getIconeRes() != 0) {
            h.img.setImageResource(c.getIconeRes());
        }
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() { return categorias.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvNome;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgIcone);
            tvNome = v.findViewById(R.id.tvNome);
        }
    }
}