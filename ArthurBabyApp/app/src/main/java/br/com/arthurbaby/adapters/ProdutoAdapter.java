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

        // Se tiver promoção (preço < 50), mostra preço antigo riscado
        boolean emPromocao = p.getPreco().doubleValue() < 50 && p.temEstoque();
        if (emPromocao) {
            h.tvPrecoAntigo.setVisibility(View.VISIBLE);
            h.tvPrecoAntigo.setText("R$ " + String.format("%.2f", p.getPreco().doubleValue() * 1.3));
            h.tvPrecoAntigo.setPaintFlags(h.tvPrecoAntigo.getPaintFlags()
                    | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.tvPrecoAntigo.setVisibility(View.GONE);
        }

        // Selo promoção
        h.tvSeloPromocao.setVisibility(emPromocao ? View.VISIBLE : View.GONE);

        // Selo esgotado
        h.tvSeloEsgotado.setVisibility(p.isEsgotado() ? View.VISIBLE : View.GONE);

        // Imagem — usa cor da categoria como placeholder
        h.img.setBackgroundColor(0xFFFAD1DE);

        // Estrelas (mock 5)
        h.tvEstrelas.setText("★★★★★");
        h.tvAvaliacao.setText("(5)");

        // Coração
        boolean fav = FavoritoRepository.getInstance().isFavorito(p);
        h.btnFavorito.setImageResource(fav ? R.drawable.ic_favorito_preenchido : R.drawable.ic_favorito);
        h.btnFavorito.setColorFilter(ContextCompat.getColor(
                h.itemView.getContext(),
                fav ? R.color.coracao_ativo : R.color.coracao_inativo));

        h.btnFavorito.setOnClickListener(v -> {
            FavoritoRepository.getInstance().toggle(p);
            boolean agora = FavoritoRepository.getInstance().isFavorito(p);
            h.btnFavorito.setImageResource(agora ? R.drawable.ic_favorito_preenchido : R.drawable.ic_favorito);
            h.btnFavorito.setColorFilter(ContextCompat.getColor(
                    v.getContext(),
                    agora ? R.color.coracao_ativo : R.color.coracao_inativo));
        });

        h.itemView.setOnClickListener(v -> {
            if (p.isEsgotado()) {
                Toast.makeText(v.getContext(), "Produto esgotado", Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onClick(p);
        });
    }

    @Override
    public int getItemCount() { return produtos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img, btnFavorito;
        TextView tvNome, tvPreco, tvPrecoAntigo, tvEstrelas, tvAvaliacao,
                tvSeloPromocao, tvSeloEsgotado;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgProduto);
            btnFavorito = v.findViewById(R.id.btnFavorito);
            tvNome = v.findViewById(R.id.tvNome);
            tvPreco = v.findViewById(R.id.tvPreco);
            tvPrecoAntigo = v.findViewById(R.id.tvPrecoAntigo);
            tvEstrelas = v.findViewById(R.id.tvEstrelas);
            tvAvaliacao = v.findViewById(R.id.tvAvaliacao);
            tvSeloPromocao = v.findViewById(R.id.tvSeloPromocao);
            tvSeloEsgotado = v.findViewById(R.id.tvSeloEsgotado);
        }
    }
}