package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Endereco;

public class EnderecoAdapter extends RecyclerView.Adapter<EnderecoAdapter.VH> {

    public interface Listener {
        void onRemover(Endereco e);
    }

    private final List<Endereco> enderecos;
    private final Listener listener;

    public EnderecoAdapter(List<Endereco> enderecos, Listener listener) {
        this.enderecos = enderecos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_endereco, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Endereco e = enderecos.get(position);
        h.tvResumo.setText(e.resumo());
        h.tvPrincipal.setVisibility(e.isPrincipal() ? View.VISIBLE : View.GONE);
        h.btnRemover.setOnClickListener(v -> listener.onRemover(e));
    }

    @Override
    public int getItemCount() { return enderecos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvResumo, tvPrincipal, btnRemover;

        VH(View v) {
            super(v);
            tvResumo = v.findViewById(R.id.tvResumo);
            tvPrincipal = v.findViewById(R.id.tvPrincipal);
            btnRemover = v.findViewById(R.id.btnRemover);
        }
    }
}