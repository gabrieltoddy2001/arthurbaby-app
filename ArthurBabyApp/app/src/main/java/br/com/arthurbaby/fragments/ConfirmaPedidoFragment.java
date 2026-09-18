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
import java.util.List;
import java.util.Locale;
import java.util.Random;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.repositories.CarrinhoRepository;

public class ConfirmaPedidoFragment extends Fragment {

    private static final String ARG_FORMA = "forma";
    private static final String ARG_OBS = "obs";

    public static ConfirmaPedidoFragment newInstance(String formaRecebimento, String observacao) {
        ConfirmaPedidoFragment f = new ConfirmaPedidoFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FORMA, formaRecebimento);
        b.putString(ARG_OBS, observacao);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_confirma_pedido, container, false);

        String forma = getArguments() != null ? getArguments().getString(ARG_FORMA) : "RETIRADA_LOJA";
        String obs = getArguments() != null ? getArguments().getString(ARG_OBS) : "";

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        int numero = 1000 + new Random().nextInt(9000);
        TextView tvNumero = v.findViewById(R.id.tvNumeroPedido);
        tvNumero.setText("Pedido #" + numero);

        // Forma de recebimento (aproveitamos o texto "Obrigado")
        TextView tvObrigado = v.findViewById(R.id.tvObrigado);
        String recebTxt = "ENTREGA".equals(forma) ? "Entrega" : "Retirada na loja";
        tvObrigado.setText("Forma de recebimento: " + recebTxt
                + (obs.isEmpty() ? "" : "\nObservação: " + obs)
                + "\n\nObrigado pela compra!");

        CarrinhoRepository repo = CarrinhoRepository.getInstance();
        TextView tvSubtotal = v.findViewById(R.id.tvSubtotalConf);
        TextView tvFrete = v.findViewById(R.id.tvFreteConf);
        TextView tvTotal = v.findViewById(R.id.tvTotal);

        tvSubtotal.setText(nf.format(repo.getSubtotal()));
        tvFrete.setText(repo.getFrete() == 0 ? "Grátis" : nf.format(repo.getFrete()));
        tvTotal.setText(nf.format(repo.getTotal()));

        LinearLayout containerItens = v.findViewById(R.id.containerItens);
        List<ItemCarrinho> itens = repo.getItens();
        for (ItemCarrinho item : itens) {
            TextView linha = new TextView(getContext());
            String texto = item.getQuantidade() + "x  " + item.getProduto().getNome()
                    + (item.getVariacao() != null ? "\n" + item.getVariacao().resumo() : "")
                    + "\n" + nf.format(item.getSubtotal());
            linha.setText(texto);
            linha.setTextColor(0xFF000000);
            linha.setTextSize(14f);
            linha.setPadding(0, 8, 0, 8);
            containerItens.addView(linha);
        }

        v.findViewById(R.id.btnVoltarHome).setOnClickListener(x -> {
            repo.limpar();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, new BrandingFinalFragment())
                    .commit();
        });

        return v;
    }
}