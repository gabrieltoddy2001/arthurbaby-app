package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.VH> {

    public interface OnClick {
        void onClick(Produto p);
    }

    private final List<Produto> produtos;
    private final OnClick listener;

    public ProdutoAdapter(List<Produto> produtos, OnClick listener) {
        this.produtos = produtos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Produto p = produtos.get(position);
        h.tvNome.setText(p.getNome());

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        h.tvPreco.setText(nf.format(p.getPreco()));

        h.img.setBackgroundColor(0xFF37B6B0);

        // Selo de promoção
        boolean emPromocao = p.getPreco().doubleValue() < 50 && p.temEstoque();
        h.tvSeloPromocao.setVisibility(emPromocao ? View.VISIBLE : View.GONE);

        // Selo de esgotado
        h.tvSeloEsgotado.setVisibility(p.isEsgotado() ? View.VISIBLE : View.GONE);

        // Coração
        boolean fav = FavoritoRepository.getInstance().isFavorito(p);
        h.btnFavorito.setText(fav ? "♥" : "♡");

        h.btnFavorito.setOnClickListener(v -> {
            FavoritoRepository.getInstance().toggle(p);
            boolean agora = FavoritoRepository.getInstance().isFavorito(p);
            h.btnFavorito.setText(agora ? "♥" : "♡");
        });

        // Clique no card — bloqueia se esgotado
        h.itemView.setOnClickListener(v -> {
            if (p.isEsgotado()) {
                android.widget.Toast.makeText(v.getContext(),
                        "Produto esgotado", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvNome, tvPreco, btnFavorito, tvSeloPromocao, tvSeloEsgotado;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgProduto);
            tvNome = v.findViewById(R.id.tvNome);
            tvPreco = v.findViewById(R.id.tvPreco);
            btnFavorito = v.findViewById(R.id.btnFavorito);
            tvSeloPromocao = v.findViewById(R.id.tvSeloPromocao);
            tvSeloEsgotado = v.findViewById(R.id.tvSeloEsgotado);
        }
    }
}