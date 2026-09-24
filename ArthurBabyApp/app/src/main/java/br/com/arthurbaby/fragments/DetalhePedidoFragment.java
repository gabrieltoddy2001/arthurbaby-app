package br.com.arthurbaby.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.CancelamentoRequest;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.utils.LoadingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalhePedidoFragment extends Fragment {

    private static final String ARG_PEDIDO = "pedido";
    private Pedido pedido;

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

        if (getArguments() != null) {
            pedido = (Pedido) getArguments().getSerializable(ARG_PEDIDO);
        }
        if (pedido == null) return v;

        // Mostra imediatamente os dados do Bundle
        preencherUI(v);

        // Botão voltar
        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Busca dados atualizados no backend
        carregarPedidoAtualizado(v);

        return v;
    }

    /**
     * Preenche a UI com o pedido atual.
     */
    private void preencherUI(View v) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        tvTitulo.setText("Pedido #" + pedido.getNumeroPedido());

        TextView tvStatus = v.findViewById(R.id.tvStatus);
        tvStatus.setText(pedido.getStatusAtual());

        TextView tvTotal = v.findViewById(R.id.tvTotal);
        tvTotal.setText("Total: " + nf.format(pedido.getTotal()));

        // Itens
        LinearLayout cItens = v.findViewById(R.id.containerItens);
        if (cItens != null) {
            cItens.removeAllViews();
            for (ItemCarrinho item : pedido.getItens()) {
                TextView linha = new TextView(getContext());
                linha.setText(item.getQuantidade() + "x  " + item.getProduto().getNome()
                        + "  —  " + nf.format(item.getSubtotal()));
                linha.setTextColor(0xFF000000);
                linha.setTextSize(14f);
                linha.setPadding(0, 6, 0, 6);
                cItens.addView(linha);
            }
        }

        // Histórico
        LinearLayout cHist = v.findViewById(R.id.containerHistorico);
        if (cHist != null) {
            cHist.removeAllViews();
            for (PedidoStatus ps : pedido.getHistorico()) {
                TextView linha = new TextView(getContext());
                linha.setText("• " + ps.getStatus() + "  —  " + sdf.format(ps.getData()));
                linha.setTextColor(0xFF000000);
                linha.setTextSize(14f);
                linha.setPadding(0, 6, 0, 6);
                cHist.addView(linha);
            }
        }

        // Botão cancelar
        com.google.android.material.button.MaterialButton btnCancelar =
                v.findViewById(R.id.btnCancelarPedido);
        if (btnCancelar != null) {
            String status = pedido.getStatusAtual();
            boolean podeCancelar = !"CANCELADO".equals(status)
                    && !"ENTREGUE".equals(status)
                    && !"EM_TRANSPORTE".equals(status);

            if (podeCancelar) {
                btnCancelar.setVisibility(View.VISIBLE);
                btnCancelar.setOnClickListener(x -> abrirDialogCancelamento());
            } else {
                btnCancelar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Busca o pedido atualizado no backend e atualiza a UI.
     */
    private void carregarPedidoAtualizado(View v) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.buscarPedidoPorNumero(pedido.getNumeroPedido())
                .enqueue(new Callback<PedidoResponse>() {
                    @Override
                    public void onResponse(Call<PedidoResponse> call,
                                           Response<PedidoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Pedido atualizado = Conversor.paraPedido(response.body());
                            if (atualizado != null) {
                                pedido = atualizado;
                                preencherUI(v);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PedidoResponse> call, Throwable t) {
                        // Silencioso — mantém os dados do Bundle
                    }
                });
    }

    private void abrirDialogCancelamento() {
        EditText etMotivo = new EditText(getContext());
        etMotivo.setHint("Motivo do cancelamento");
        etMotivo.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar pedido")
                .setMessage("Tem certeza que deseja cancelar este pedido?")
                .setView(etMotivo)
                .setPositiveButton("Confirmar", (d, w) -> {
                    String motivo = etMotivo.getText().toString().trim();
                    if (motivo.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Informe o motivo do cancelamento", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cancelarPedido(motivo);
                })
                .setNegativeButton("Voltar", null)
                .show();
    }

    private void cancelarPedido(String motivo) {
        ApiService api = RetrofitClient.getApi(requireContext());
        LoadingUtils.mostrar(requireContext());

        api.cancelarPedido(pedido.getNumeroPedido(), new CancelamentoRequest(motivo))
                .enqueue(new Callback<PedidoResponse>() {
                    @Override
                    public void onResponse(Call<PedidoResponse> call,
                                           Response<PedidoResponse> response) {
                        LoadingUtils.esconder();

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Pedido cancelado", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            String msg = "Erro ao cancelar pedido";
                            try {
                                if (response.errorBody() != null) {
                                    String erroJson = response.errorBody().string();
                                    if (erroJson.contains("\"erro\"")) {
                                        int inicio = erroJson.indexOf("\"erro\"") + 8;
                                        int fim = erroJson.indexOf("\"", inicio);
                                        msg = erroJson.substring(inicio, fim);
                                    }
                                }
                            } catch (Exception ignored) {}
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PedidoResponse> call, Throwable t) {
                        LoadingUtils.esconder();
                        Toast.makeText(requireContext(),
                                "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}