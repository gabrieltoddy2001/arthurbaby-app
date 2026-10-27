package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import br.com.arthurbaby.R;
import br.com.arthurbaby.network.dto.CupomResponse;

public class CupomAdapter extends RecyclerView.Adapter<CupomAdapter.VH> {
    private final List<CupomResponse> cupons;

    public CupomAdapter(List<CupomResponse> cupons) { this.cupons = cupons; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cupom, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CupomResponse c = cupons.get(pos);
        h.tvCodigo.setText(c.codigo);
        h.tvDescricao.setText(c.descricao);
        if (c.validoAte != null) {
            h.tvValidade.setText("Válido até: " + c.validoAte.split("T")[0]);
            h.tvValidade.setVisibility(View.VISIBLE);
        } else {
            h.tvValidade.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return cupons.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCodigo, tvDescricao, tvValidade;
        VH(View v) {
            super(v);
            tvCodigo = v.findViewById(R.id.tvCodigo);
            tvDescricao = v.findViewById(R.id.tvDescricao);
            tvValidade = v.findViewById(R.id.tvValidade);
        }
    }
}