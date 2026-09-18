package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ItemCarrinhoAdapter;
import br.com.arthurbaby.repositories.CarrinhoRepository;

public class CarrinhoFragment extends Fragment {

    private RecyclerView rvCarrinho;
    private TextView tvSubtotal, tvFrete, tvTotal, tvVazio, tvContador;
    private ItemCarrinhoAdapter adapter;
    private CarrinhoRepository repo;
    private double descontoAplicado = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_carrinho, container, false);

        repo = CarrinhoRepository.getInstance();

        rvCarrinho = v.findViewById(R.id.rvCarrinho);
        tvSubtotal = v.findViewById(R.id.tvSubtotal);
        tvFrete = v.findViewById(R.id.tvFrete);
        tvTotal = v.findViewById(R.id.tvTotal);
        tvVazio = v.findViewById(R.id.tvVazio);
        tvContador = v.findViewById(R.id.tvContadorItens);

        rvCarrinho.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemCarrinhoAdapter(repo.getItens(), this::atualizarTotal);
        rvCarrinho.setAdapter(adapter);

        EditText etObservacao = v.findViewById(R.id.etObservacao);
        EditText etCupom = v.findViewById(R.id.etCupom);
        RadioGroup rgRecebimento = v.findViewById(R.id.rgRecebimento);
        RadioButton rbEntrega = v.findViewById(R.id.rbEntrega);
        RadioButton rbRetirada = v.findViewById(R.id.rbRetirada);
        LinearLayout rowDesconto = v.findViewById(R.id.rowDesconto);
        TextView tvDesconto = v.findViewById(R.id.tvDesconto);

        if ("ENTREGA".equals(repo.getFormaRecebimento())) rbEntrega.setChecked(true);
        else rbRetirada.setChecked(true);

        rgRecebimento.setOnCheckedChangeListener((group, checkedId) -> {
            repo.setFormaRecebimento(checkedId == R.id.rbEntrega ? "ENTREGA" : "RETIRADA_LOJA");
            atualizarTotal();
        });

        // Aplicar cupom
        v.findViewById(R.id.btnAplicarCupom).setOnClickListener(x -> {
            String cupom = etCupom.getText().toString().trim().toUpperCase();
            if ("ARTHUR10".equals(cupom)) {
                descontoAplicado = repo.getSubtotal() * 0.10;
                rowDesconto.setVisibility(View.VISIBLE);
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                tvDesconto.setText("- " + nf.format(descontoAplicado));
                Toast.makeText(requireContext(), "Cupom aplicado! 10% de desconto",
                        Toast.LENGTH_SHORT).show();
            } else {
                descontoAplicado = 0;
                rowDesconto.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Cupom inválido",
                        Toast.LENGTH_SHORT).show();
            }
            atualizarTotal();
        });

        // Finalizar
        v.findViewById(R.id.btnFinalizar).setOnClickListener(x -> {
            if (repo.getItens().isEmpty()) {
                Toast.makeText(requireContext(), "Carrinho vazio!", Toast.LENGTH_SHORT).show();
                return;
            }
            String observacao = etObservacao.getText().toString().trim();
            String forma = repo.getFormaRecebimento();

            br.com.arthurbaby.utils.LoadingUtils.mostrar(requireContext());

            new android.os.Handler().postDelayed(() -> {
                br.com.arthurbaby.utils.LoadingUtils.esconder();

                ConfirmaPedidoFragment frag = ConfirmaPedidoFragment.newInstance(forma, observacao);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, frag)
                        .addToBackStack(null)
                        .commit();
            }, 900);
        });

        atualizarTotal();
        return v;
    }

    private void atualizarTotal() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double subtotal = repo.getSubtotal();
        double frete = repo.getFrete();
        double total = subtotal + frete - descontoAplicado;

        tvSubtotal.setText(nf.format(subtotal));
        tvFrete.setText(frete == 0 ? "Grátis" : nf.format(frete));
        tvTotal.setText(nf.format(total));
        tvVazio.setVisibility(repo.getItens().isEmpty() ? View.VISIBLE : View.GONE);
        tvContador.setText(repo.getTotalItens() + (repo.getTotalItens() == 1 ? " item" : " itens"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) atualizarTotal();
    }
}