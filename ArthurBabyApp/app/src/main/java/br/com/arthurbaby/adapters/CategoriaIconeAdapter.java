package br.com.arthurbaby.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
        if (parent instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) parent).getLayoutManager();
            boolean linha = lm instanceof androidx.recyclerview.widget.LinearLayoutManager
                    && !(lm instanceof androidx.recyclerview.widget.GridLayoutManager)
                    && ((androidx.recyclerview.widget.LinearLayoutManager) lm).getOrientation()
                            == RecyclerView.HORIZONTAL;
            if (linha) {
                v.getLayoutParams().width = larguraItemEmLinha((RecyclerView) parent);
            }
        }
        return new VH(v);
    }

    /**
     * Largura de cada categoria no modo "uma linha": divide a área visível em partes iguais
     * para que, ao abrir a tela, apareçam 5 categorias inteiras (nenhuma cortada na borda).
     * Em telas estreitas, onde cada uma ficaria com menos de 64dp, mostra 4.
     */
    private static int larguraItemEmLinha(RecyclerView rv) {
        float densidade = rv.getResources().getDisplayMetrics().density;
        int largura = rv.getWidth() > 0 ? rv.getWidth()
                : rv.getResources().getDisplayMetrics().widthPixels;
        int util = largura - rv.getPaddingLeft() - rv.getPaddingRight();
        int porTela = util / 5 >= Math.round(64 * densidade) ? 5 : 4;
        return util / porTela;
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Categoria c = categorias.get(position);
        h.tvNome.setText(c.getNome());

        if (c.getIconeRes() != 0) {
            h.img.setImageResource(c.getIconeRes());
        }

        // Cor do círculo baseada na categoria
        int corFundo = corPorCategoria(c.getNome());
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(corFundo);
        h.frameIcone.setBackground(bg);

        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    private int corPorCategoria(String nome) {
        if (nome == null) return 0xFFE6F7F6;
        String n = nome.toLowerCase();
        if (n.contains("enxoval")) return 0xFFE6F7F6;
        if (n.contains("bebê") || n.contains("bebe")) return 0xFFFDF0F4;
        if (n.contains("infantil")) return 0xFFFFF4E0;
        if (n.contains("acess")) return 0xFFF0E6FA;
        if (n.contains("higiene")) return 0xFFE3F5EE;
        if (n.contains("aliment")) return 0xFFFCEFE6;
        if (n.contains("quarto")) return 0xFFE6F0FB;
        if (n.contains("presente")) return 0xFFFFE6E2;
        if (n.contains("promo")) return 0xFFFFE9EE;
        if (n.contains("kit")) return 0xFFEFEFEF;
        return 0xFFF5EEF2;
    }

    @Override
    public int getItemCount() { return categorias.size(); }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout frameIcone;
        ImageView img;
        TextView tvNome;

        VH(View v) {
            super(v);
            frameIcone = v.findViewById(R.id.frameIcone);
            img = v.findViewById(R.id.imgIcone);
            tvNome = v.findViewById(R.id.tvNome);
        }
    }
}