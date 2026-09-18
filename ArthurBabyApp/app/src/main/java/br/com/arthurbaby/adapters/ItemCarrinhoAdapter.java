package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.ItemCarrinho;

public class ItemCarrinhoAdapter extends RecyclerView.Adapter<ItemCarrinhoAdapter.VH> {

    public interface Listener {
        void onAlterar();
    }

    private final List<ItemCarrinho> itens;
    private final Listener listener;

    public ItemCarrinhoAdapter(List<ItemCarrinho> itens, Listener listener) {
        this.itens = itens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrinho, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ItemCarrinho item = itens.get(position);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        h.tvNome.setText(item.getProduto().getNome());
        h.tvPreco.setText(nf.format(item.getProduto().getPreco()));
        h.tvQtd.setText(String.valueOf(item.getQuantidade()));

        if (item.getVariacao() != null) {
            h.tvVariacao.setVisibility(View.VISIBLE);
            h.tvVariacao.setText(item.getVariacao().resumo());
        } else {
            h.tvVariacao.setVisibility(View.GONE);
        }

        // Imagem — cor da categoria como placeholder
        h.img.setBackgroundColor(0xFFFAD1DE);

        // Botão remover vermelho
        h.btnRemover.setImageResource(R.drawable.ic_sair);
        h.btnRemover.setColorFilter(ContextCompat.getColor(
                h.itemView.getContext(), R.color.vermelho_desconto));

        h.btnMais.setOnClickListener(v -> {
            item.setQuantidade(item.getQuantidade() + 1);
            notifyItemChanged(position);
            listener.onAlterar();
        });

        h.btnMenos.setOnClickListener(v -> {
            if (item.getQuantidade() > 1) {
                item.setQuantidade(item.getQuantidade() - 1);
                notifyItemChanged(position);
                listener.onAlterar();
            } else {
                itens.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, itens.size());
                listener.onAlterar();
            }
        });

        h.btnRemover.setOnClickListener(v -> {
            itens.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itens.size());
            listener.onAlterar();
        });
    }

    @Override
    public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img, btnRemover;
        TextView tvNome, tvVariacao, tvPreco, tvQtd;
        com.google.android.material.button.MaterialButton btnMais, btnMenos;

        VH(View v) {
            super(v);
            img = v.findViewById(R.id.imgProduto);
            btnRemover = v.findViewById(R.id.btnRemover);
            tvNome = v.findViewById(R.id.tvNome);
            tvVariacao = v.findViewById(R.id.tvVariacao);
            tvPreco = v.findViewById(R.id.tvPreco);
            tvQtd = v.findViewById(R.id.tvQtd);
            btnMais = v.findViewById(R.id.btnMais);
            btnMenos = v.findViewById(R.id.btnMenos);
        }
    }
}