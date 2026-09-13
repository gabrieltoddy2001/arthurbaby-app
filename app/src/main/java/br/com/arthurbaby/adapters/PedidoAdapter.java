package br.com.arthurbaby.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Pedido;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.VH> {

    public interface OnClick {
        void onClick(Pedido p);
    }

    private final List<Pedido> pedidos;
    private final OnClick listener;

    public PedidoAdapter(List<Pedido> pedidos, OnClick listener) {
        this.pedidos = pedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Pedido p = pedidos.get(position);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

        h.tvNumero.setText("Pedido #" + p.getNumero());
        h.tvStatus.setText(p.getStatusAtual());
        h.tvData.setText(sdf.format(p.getData()));
        h.tvTotal.setText(nf.format(p.getTotal()));

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNumero, tvStatus, tvData, tvTotal;

        VH(View v) {
            super(v);
            tvNumero = v.findViewById(R.id.tvNumero);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvData = v.findViewById(R.id.tvData);
            tvTotal = v.findViewById(R.id.tvTotal);
        }
    }
}
