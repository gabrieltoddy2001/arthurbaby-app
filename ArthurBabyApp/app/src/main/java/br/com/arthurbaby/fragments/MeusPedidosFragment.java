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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeusPedidosFragment extends Fragment {

    private TabLayout tabs;
    private ViewPager2 pager;

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

        carregarPedidos();
        return v;
    }

    private void carregarPedidos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) {
            Toast.makeText(requireContext(),
                    "Faça login novamente", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarPedidosCliente(clienteId).enqueue(new Callback<List<PedidoResponse>>() {
            @Override
            public void onResponse(Call<List<PedidoResponse>> call,
                                   Response<List<PedidoResponse>> response) {
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
                            "Erro ao carregar pedidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PedidoResponse>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}