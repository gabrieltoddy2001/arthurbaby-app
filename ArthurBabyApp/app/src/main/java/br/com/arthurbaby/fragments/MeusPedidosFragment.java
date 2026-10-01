package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

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

        if (!AuthGuard.estaLogado(requireContext())) {
            AuthGuard.mostrarDialogLogin(requireActivity(),
                    "Entre para ver seus pedidos.");
            requireActivity().getSupportFragmentManager().popBackStack();
            return v;
        }

        carregarPedidos();
        return v;
    }

    private void carregarPedidos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) {
            AuthGuard.mostrarDialogLogin(requireActivity(),
                    "Entre para ver seus pedidos.");
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        LoadingView.mostrar(progressBar, containerPedidos);

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarPedidosCliente(clienteId).enqueue(new Callback<List<PedidoResponse>>() {
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