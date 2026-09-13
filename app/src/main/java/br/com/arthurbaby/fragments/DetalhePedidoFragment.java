package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Pedido;
import br.com.arthurbaby.models.PedidoStatus;

public class DetalhePedidoFragment extends Fragment {

    private static final String ARG_PEDIDO = "pedido";

    public static DetalhePedidoFragment newInstance(Pedido p) {
        DetalhePedidoFragment f = new DetalhePedidoFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_PEDIDO, p);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalhe_pedido, container, false);

        Pedido pedido = null;
        if (getArguments() != null) {
            pedido = (Pedido) getArguments().getSerializable(ARG_PEDIDO);
        }
        if (pedido == null) return v;

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        tvTitulo.setText("Pedido #" + pedido.getNumero());

        TextView tvStatus = v.findViewById(R.id.tvStatus);
        tvStatus.setText(pedido.getStatusAtual());

        TextView tvTotal = v.findViewById(R.id.tvTotal);
        tvTotal.setText("Total: " + nf.format(pedido.getTotal()));

        // Itens
        LinearLayout cItens = v.findViewById(R.id.containerItens);
        for (ItemCarrinho item : pedido.getItens()) {
            TextView linha = new TextView(getContext());
            linha.setText(item.getQuantidade() + "x  " + item.getProduto().getNome()
                    + "  —  " + nf.format(item.getSubtotal()));
            linha.setTextColor(0xFF000000);
            linha.setTextSize(14f);
            linha.setPadding(0, 6, 0, 6);
            cItens.addView(linha);
        }

        // Histórico
        LinearLayout cHist = v.findViewById(R.id.containerHistorico);
        for (PedidoStatus ps : pedido.getHistorico()) {
            TextView linha = new TextView(getContext());
            linha.setText("• " + ps.getStatus() + "  —  " + sdf.format(ps.getData()));
            linha.setTextColor(0xFF000000);
            linha.setTextSize(14f);
            linha.setPadding(0, 6, 0, 6);
            cHist.addView(linha);
        }

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return v;
    }
}