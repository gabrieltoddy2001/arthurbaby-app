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
import java.util.Locale;

import br.com.arthurbaby.R;

public class ConfirmaPedidoFragment extends Fragment {

    private static final String ARG_NUMERO = "numero";
    private static final String ARG_FORMA = "forma";
    private static final String ARG_OBS = "obs";
    private static final String ARG_TOTAL = "total";
    private static final String ARG_SUBTOTAL = "subtotal";
    private static final String ARG_FRETE = "frete";
    private static final String ARG_DESCONTO = "desconto";
    private static final String ARG_CUPOM = "cupom";

    public static ConfirmaPedidoFragment newInstance(String numeroPedido, String formaRecebimento,
                                                     String observacao, double total,
                                                     double subtotal, double frete,
                                                     double desconto, String cupom) {
        ConfirmaPedidoFragment f = new ConfirmaPedidoFragment();
        Bundle b = new Bundle();
        b.putString(ARG_NUMERO, numeroPedido);
        b.putString(ARG_FORMA, formaRecebimento);
        b.putString(ARG_OBS, observacao);
        b.putDouble(ARG_TOTAL, total);
        b.putDouble(ARG_SUBTOTAL, subtotal);
        b.putDouble(ARG_FRETE, frete);
        b.putDouble(ARG_DESCONTO, desconto);
        b.putString(ARG_CUPOM, cupom);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_confirma_pedido, container, false);

        Bundle args = getArguments();
        String numero = args != null ? args.getString(ARG_NUMERO, "") : "";
        String forma = args != null ? args.getString(ARG_FORMA, "RETIRADA_LOJA") : "";
        String obs = args != null ? args.getString(ARG_OBS, "") : "";
        double total = args != null ? args.getDouble(ARG_TOTAL, 0) : 0;
        double subtotal = args != null ? args.getDouble(ARG_SUBTOTAL, 0) : 0;
        double frete = args != null ? args.getDouble(ARG_FRETE, 0) : 0;
        double desconto = args != null ? args.getDouble(ARG_DESCONTO, 0) : 0;
        String cupom = args != null ? args.getString(ARG_CUPOM, null) : null;

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        TextView tvNumero = v.findViewById(R.id.tvNumeroPedido);
        tvNumero.setText("Pedido #" + numero);

        TextView tvObrigado = v.findViewById(R.id.tvObrigado);
        String recebTxt = "ENTREGA".equals(forma) ? "Entrega" : "Retirada na loja";
        tvObrigado.setText("Forma de recebimento: " + recebTxt
                + (obs.isEmpty() ? "" : "\nObservação: " + obs)
                + "\n\nVocê receberá atualizações sobre o pedido");

        // Subtotal
        TextView tvSub = v.findViewById(R.id.tvSubtotalConf);
        if (tvSub != null) tvSub.setText(nf.format(subtotal));

        // Desconto (só aparece se > 0)
        LinearLayout rowDesc = v.findViewById(R.id.rowDescontoConf);
        TextView tvLabelDesc = v.findViewById(R.id.tvLabelDescontoConf);
        TextView tvDescV = v.findViewById(R.id.tvDescontoConf);
        if (rowDesc != null && tvDescV != null) {
            if (desconto > 0) {
                rowDesc.setVisibility(View.VISIBLE);
                if (tvLabelDesc != null) {
                    tvLabelDesc.setText(cupom != null && !cupom.isEmpty()
                            ? "Desconto (" + cupom + ")"
                            : "Desconto");
                }
                tvDescV.setText("- " + nf.format(desconto));
            } else {
                rowDesc.setVisibility(View.GONE);
            }
        }

        // Frete
        TextView tvFreteV = v.findViewById(R.id.tvFreteConf);
        if (tvFreteV != null) tvFreteV.setText(frete == 0 ? "Grátis" : nf.format(frete));

        // Total
        TextView tvTotalV = v.findViewById(R.id.tvTotal);
        if (tvTotalV != null) tvTotalV.setText(nf.format(total));

        v.findViewById(R.id.btnVoltarHome).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new BrandingFinalFragment())
                        .commit());

        return v;
    }
}