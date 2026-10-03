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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Pedido;
import br.com.arthurbaby.models.PedidoStatus;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.CancelamentoRequest;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MoedaUtils;
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

        preencherUI(v);
        carregarPedidoAtualizado(v);

        return v;
    }

    private void preencherUI(View v) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        int corTexto = ContextCompat.getColor(requireContext(), R.color.texto_primario);

        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        tvTitulo.setText("Pedido #" + pedido.getNumeroPedido());

        TextView tvStatus = v.findViewById(R.id.tvStatus);
        tvStatus.setText(pedido.getStatusAtual());

        TextView tvTotal = v.findViewById(R.id.tvTotal);
        tvTotal.setText("Total: " + MoedaUtils.formatar(pedido.getTotal()));

        LinearLayout cItens = v.findViewById(R.id.containerItens);
        if (cItens != null) {
            cItens.removeAllViews();
            for (ItemCarrinho item : pedido.getItens()) {
                TextView linha = new TextView(getContext());
                linha.setText(item.getQuantidade() + "x  " + item.getProduto().getNome()
                        + "  —  " + MoedaUtils.formatar(item.getSubtotal()));
                linha.setTextColor(corTexto);
                linha.setTextSize(14f);
                linha.setPadding(0, 6, 0, 6);
                cItens.addView(linha);
            }
        }

        LinearLayout cHist = v.findViewById(R.id.containerHistorico);
        if (cHist != null) {
            cHist.removeAllViews();
            for (PedidoStatus ps : pedido.getHistorico()) {
                TextView linha = new TextView(getContext());
                linha.setText("• " + ps.getStatus() + "  —  " + sdf.format(ps.getData()));
                linha.setTextColor(corTexto);
                linha.setTextSize(14f);
                linha.setPadding(0, 6, 0, 6);
                cHist.addView(linha);
            }
        }

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

    private void carregarPedidoAtualizado(View v) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.buscarPedidoPorNumero(pedido.getNumeroPedido())
                .enqueue(new Callback<PedidoResponse>() {
                    @Override
                    public void onResponse(Call<PedidoResponse> call,
                                           Response<PedidoResponse> response) {
                        if (!isAdded() || getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Pedido atualizado = br.com.arthurbaby.network.Conversor
                                    .paraPedido(response.body());
                            if (atualizado != null) {
                                pedido = atualizado;
                                preencherUI(v);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PedidoResponse> call, Throwable t) {
                        // Silencioso
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
    }
}