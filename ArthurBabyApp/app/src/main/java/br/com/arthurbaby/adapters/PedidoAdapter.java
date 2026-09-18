package br.com.arthurbaby.adapters;

import android.graphics.drawable.GradientDrawable;
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
        h.tvStatus.setText(rotuloStatus(p.getStatusAtual()));
        h.tvData.setText(sdf.format(p.getData()));
        h.tvTotal.setText(nf.format(p.getTotal()));

        // Cor do status
        int cor = corStatus(p.getStatusAtual());
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(20f);
        bg.setColor(cor);
        h.tvStatus.setBackground(bg);

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    private String rotuloStatus(String status) {
        switch (status) {
            case "PEDIDO_GERADO": return "Gerado";
            case "EM_ANALISE": return "Em análise";
            case "AGUARDANDO_CONFIRMACAO": return "Aguardando";
            case "CONFIRMADO": return "Confirmado";
            case "SEPARANDO_PRODUTOS": return "Separando";
            case "PRONTO_PARA_RETIRADA": return "Pronto";
            case "EM_TRANSPORTE": return "A caminho";
            case "ENTREGUE": return "Entregue";
            case "CANCELADO": return "Cancelado";
            default: return status;
        }
    }

    private int corStatus(String status) {
        switch (status) {
            case "ENTREGUE": return 0xFF10B981;       // verde
            case "EM_TRANSPORTE":
            case "SEPARANDO_PRODUTOS": return 0xFF3B82F6; // azul
            case "AGUARDANDO_CONFIRMACAO":
            case "EM_ANALISE": return 0xFFFFC107;      // amarelo
            case "CANCELADO": return 0xFFEF4444;       // vermelho
            case "CONFIRMADO":
            case "PRONTO_PARA_RETIRADA": return 0xFF37B6B0;
            default: return 0xFF94A3B8;
        }
    }

    @Override
    public int getItemCount() { return pedidos.size(); }

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