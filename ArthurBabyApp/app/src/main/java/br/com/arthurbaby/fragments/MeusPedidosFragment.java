package br.com.arthurbaby.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.PedidosPagerAdapter;
import br.com.arthurbaby.models.Pedido;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeusPedidosFragment extends Fragment {

    private TabLayout tabs;
    private ViewPager2 pager;
    private View progressBar, containerPedidos;
    private View containerFiltro;
    private TextView tvFiltroAtivo, tvLimparFiltro;

    private String dataInicio = null;
    private String dataFim = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_meus_pedidos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        tabs = v.findViewById(R.id.tabsPedidos);
        pager = v.findViewById(R.id.viewPagerPedidos);
        progressBar = v.findViewById(R.id.progressBar);
        containerPedidos = v.findViewById(R.id.containerPedidos);
        containerFiltro = v.findViewById(R.id.containerFiltro);
        tvFiltroAtivo = v.findViewById(R.id.tvFiltroAtivo);
        tvLimparFiltro = v.findViewById(R.id.tvLimparFiltro);

        // Ações dos botões de filtro
        v.findViewById(R.id.btnFiltrar).setOnClickListener(x -> abrirFiltroData());
        tvLimparFiltro.setOnClickListener(x -> {
            dataInicio = null;
            dataFim = null;
            containerFiltro.setVisibility(View.GONE);
            carregarPedidos();
        });

        if (!AuthGuard.estaLogado(requireContext())) {
            AuthGuard.mostrarDialogLogin(requireActivity(), "Entre para ver seus pedidos.");
            requireActivity().getSupportFragmentManager().popBackStack();
            return v;
        }

        carregarPedidos();
        return v;
    }

    private void abrirFiltroData() {
        Calendar c = Calendar.getInstance();
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    dataInicio = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    abrirFiltroDataFim(); // Pede a data fim na sequência
                }, ano, mes, dia);

        dialog.setTitle("Data de início");
        dialog.show();
    }

    private void abrirFiltroDataFim() {
        Calendar c = Calendar.getInstance();
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    dataFim = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                    // Mostra a barrinha com o filtro ativo
                    containerFiltro.setVisibility(View.VISIBLE);
                    tvFiltroAtivo.setText("Filtro: " + dataInicio + " até " + dataFim);

                    carregarPedidos();
                }, ano, mes, dia);

        dialog.setTitle("Data de fim");
        dialog.show();
    }

    private void carregarPedidos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) {
            AuthGuard.mostrarDialogLogin(requireActivity(), "Entre para ver seus pedidos.");
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        LoadingView.mostrar(progressBar, containerPedidos);

        ApiService api = RetrofitClient.getApi(requireContext());
        // Envia as datas (se existirem) para o backend filtrar
        api.listarPedidosCliente(clienteId, null, dataInicio, dataFim)
                .enqueue(new Callback<List<PedidoResponse>>() {
                    @Override
                    public void onResponse(Call<List<PedidoResponse>> call,
                                           Response<List<PedidoResponse>> response) {
                        LoadingView.esconder(progressBar, containerPedidos);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Pedido> todos = Conversor.paraPedidos(response.body());

                            List<Pedido> emAndamento = new ArrayList<>();
                            List<Pedido> finalizados = new ArrayList<>();

                            for (Pedido p : todos) {
                                String s = p.getStatusAtual();
                                if ("ENTREGUE".equals(s) || "CANCELADO".equals(s)) {
                                    finalizados.add(p);
                                } else {
                                    emAndamento.add(p);
                                }
                            }

                            pager.setAdapter(new PedidosPagerAdapter(
                                    requireActivity(), emAndamento, finalizados));

                            new TabLayoutMediator(tabs, pager, (tab, position) ->
                                    tab.setText(position == 0 ? "Em andamento" : "Finalizado")
                            ).attach();
                        } else {
                            Toast.makeText(requireContext(),
                                    ErrorUtils.extrairMensagem(response),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PedidoResponse>> call, Throwable t) {
                        LoadingView.esconder(progressBar, containerPedidos);
                        Toast.makeText(requireContext(),
                                "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}