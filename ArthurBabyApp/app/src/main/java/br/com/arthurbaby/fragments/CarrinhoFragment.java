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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ItemCarrinhoAdapter;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.CupomValidacaoRequest;
import br.com.arthurbaby.network.dto.CupomValidacaoResponse;
import br.com.arthurbaby.network.dto.PedidoItemRequest;
import br.com.arthurbaby.network.dto.PedidoRequest;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MoedaUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarrinhoFragment extends Fragment {

    private RecyclerView rvCarrinho;
    private TextView tvSubtotal, tvFrete, tvTotal, tvContador;
    private View tvVazio;
    private ItemCarrinhoAdapter adapter;
    private CarrinhoRepository repo;
    private double descontoAplicado = 0;
    private String cupomAplicado = null;
    private boolean freteGratisPorCupom = false;

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

        // CUPOM — via API (dinâmico)
        v.findViewById(R.id.btnAplicarCupom).setOnClickListener(x -> {
            String cupom = etCupom.getText().toString().trim().toUpperCase();
            if (cupom.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o cupom", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingUtils.mostrar(requireContext());

            ApiService api = RetrofitClient.getApi(requireContext());
            api.validarCupom(new CupomValidacaoRequest(
                            cupom, BigDecimal.valueOf(repo.getSubtotal())))
                    .enqueue(new Callback<CupomValidacaoResponse>() {
                        @Override
                        public void onResponse(Call<CupomValidacaoResponse> call,
                                               Response<CupomValidacaoResponse> response) {
                            LoadingUtils.esconder();

                            if (response.isSuccessful() && response.body() != null) {
                                CupomValidacaoResponse c = response.body();

                                if (c.valido) {
                                    descontoAplicado = c.desconto != null ? c.desconto.doubleValue() : 0;
                                    cupomAplicado = c.codigo;
                                    freteGratisPorCupom = c.freteGratis;

                                    if (descontoAplicado > 0) {
                                        rowDesconto.setVisibility(View.VISIBLE);
                                        tvDesconto.setText("- " + MoedaUtils.formatar(descontoAplicado));
                                    } else {
                                        rowDesconto.setVisibility(View.GONE);
                                    }

                                    String msg = c.descricao != null ? c.descricao : "Cupom aplicado!";
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    descontoAplicado = 0;
                                    cupomAplicado = null;
                                    freteGratisPorCupom = false;
                                    rowDesconto.setVisibility(View.GONE);

                                    String motivo = c.motivo != null ? c.motivo : "Cupom inválido";
                                    Toast.makeText(requireContext(), motivo, Toast.LENGTH_SHORT).show();
                                }
                                atualizarTotal();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erro ao validar cupom", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CupomValidacaoResponse> call, Throwable t) {
                            LoadingUtils.esconder();
                            Toast.makeText(requireContext(),
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // === FINALIZAR PEDIDO ===
        v.findViewById(R.id.btnFinalizar).setOnClickListener(x -> {

            if (repo.getItens().isEmpty()) {
                Toast.makeText(requireContext(), "Carrinho vazio!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!AuthGuard.estaLogado(requireContext())) {
                AuthGuard.mostrarDialogLogin(requireActivity(),
                        "Entre para finalizar seu pedido. Seus itens foram salvos!");
                return;
            }

            Long clienteId = TokenStorage.getUsuarioId(requireContext());
            if (clienteId == null) {
                Toast.makeText(requireContext(),
                        "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();
                AuthGuard.mostrarDialogLogin(requireActivity(), null);
                return;
            }

            String observacao = etObservacao.getText().toString().trim();
            String forma = repo.getFormaRecebimento();

            List<PedidoItemRequest> itensReq = new ArrayList<>();
            for (ItemCarrinho item : repo.getItens()) {
                itensReq.add(new PedidoItemRequest(
                        item.getProduto().getId(),
                        item.getVariacaoId(),
                        item.getQuantidade()
                ));
            }

            PedidoRequest request = new PedidoRequest(
                    clienteId,
                    forma,
                    cupomAplicado,
                    BigDecimal.valueOf(descontoAplicado),
                    BigDecimal.valueOf(freteGratisPorCupom ? 0 : repo.getFrete()),
                    observacao,
                    itensReq
            );

            LoadingUtils.mostrar(requireContext());

            ApiService api = RetrofitClient.getApi(requireContext());
            api.criarPedido(request).enqueue(new Callback<PedidoResponse>() {
                @Override
                public void onResponse(Call<PedidoResponse> call, Response<PedidoResponse> response) {
                    LoadingUtils.esconder();

                    if (response.isSuccessful() && response.body() != null) {
                        PedidoResponse pedido = response.body();

                        repo.limpar();

                        ConfirmaPedidoFragment frag = ConfirmaPedidoFragment.newInstance(
                                pedido.numero,
                                forma,
                                observacao,
                                pedido.total != null ? pedido.total.doubleValue() : 0,
                                pedido.subtotal != null ? pedido.subtotal.doubleValue() : 0,
                                pedido.frete != null ? pedido.frete.doubleValue() : 0,
                                pedido.desconto != null ? pedido.desconto.doubleValue() : descontoAplicado,
                                pedido.cupom != null ? pedido.cupom : cupomAplicado
                        );
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frameContainer, frag)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(requireContext(),
                                ErrorUtils.extrairMensagem(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PedidoResponse> call, Throwable t) {
                    LoadingUtils.esconder();
                    Toast.makeText(requireContext(),
                            "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        atualizarTotal();
        return v;
    }

    private void atualizarTotal() {
        double subtotal = repo.getSubtotal();
        double frete = freteGratisPorCupom ? 0 : repo.getFrete();
        double total = subtotal + frete - descontoAplicado;

        tvSubtotal.setText(MoedaUtils.formatar(subtotal));
        tvFrete.setText(frete == 0 ? "Grátis" : MoedaUtils.formatar(frete));
        tvTotal.setText(MoedaUtils.formatar(total));
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