package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.repositories.FavoritoRepository;
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.MoedaUtils;

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
        h.tvPreco.setText(MoedaUtils.formatar(p.getPreco()));

        // Carrega a imagem com fade-in
        if (p.getImagemUrl() != null && !p.getImagemUrl().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(p.getImagemUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .placeholder(R.drawable.bg_imagem_produto)
                    .error(R.drawable.bg_imagem_produto)
                    .centerCrop()
                    .into(h.img);
        } else {
            h.img.setImageDrawable(null);
            h.img.setBackgroundColor(ContextCompat.getColor(
                    h.itemView.getContext(), R.color.placeholder_produto));
        }

        boolean emPromocao = p.getPreco().doubleValue() < 50 && p.temEstoque();
        h.tvSeloPromocao.setVisibility(emPromocao ? View.VISIBLE : View.GONE);
        h.tvSeloEsgotado.setVisibility(p.isEsgotado() ? View.VISIBLE : View.GONE);

        boolean logado = AuthGuard.estaLogado(h.itemView.getContext());
        h.btnFavorito.setVisibility(logado ? View.VISIBLE : View.GONE);

        if (logado) {
            boolean fav = FavoritoRepository.getInstance().isFavorito(p.getId());
            atualizarCoracao(h.btnFavorito, fav);

            h.btnFavorito.setOnClickListener(v -> {
                boolean agora = FavoritoRepository.getInstance().toggle(
                        v.getContext(), p.getId(), () -> {});
                atualizarCoracao(h.btnFavorito, agora);
            });
        }

        h.itemView.setOnClickListener(v -> {
            if (p.isEsgotado()) {
                Toast.makeText(v.getContext(), "Produto esgotado", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onClick(p);
        });
    }

    private void atualizarCoracao(ImageView btn, boolean fav) {
        btn.setImageResource(fav ? R.drawable.ic_favorito_preenchido : R.drawable.ic_favorito);
        btn.setColorFilter(ContextCompat.getColor(btn.getContext(),
                fav ? R.color.coracao_ativo : R.color.coracao_inativo));
    }

    @Override
    public int getItemCount() { return produtos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img, btnFavorito;
        TextView tvNome, tvPreco, tvSeloPromocao, tvSeloEsgotado;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgProduto);
            btnFavorito = v.findViewById(R.id.btnFavorito);
            tvNome = v.findViewById(R.id.tvNome);
            tvPreco = v.findViewById(R.id.tvPreco);
            tvSeloPromocao = v.findViewById(R.id.tvSeloPromocao);
            tvSeloEsgotado = v.findViewById(R.id.tvSeloEsgotado);
        }
    }
}