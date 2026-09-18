package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import br.com.arthurbaby.mock.MockData;
import br.com.arthurbaby.models.Pedido;

public class MeusPedidosFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_meus_pedidos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Divide os pedidos em andamento vs finalizados
        List<Pedido> todos = MockData.getPedidos();
        List<Pedido> emAndamento = new ArrayList<>();
        List<Pedido> finalizados = new ArrayList<>();

        for (Pedido p : todos) {
            if ("ENTREGUE".equals(p.getStatusAtual()) || "CANCELADO".equals(p.getStatusAtual())) {
                finalizados.add(p);
            } else {
                emAndamento.add(p);
            }
        }

        TabLayout tabs = v.findViewById(R.id.tabsPedidos);
        ViewPager2 pager = v.findViewById(R.id.viewPagerPedidos);

        pager.setAdapter(new PedidosPagerAdapter(requireActivity(), emAndamento, finalizados));

        new TabLayoutMediator(tabs, pager, (tab, position) ->
                tab.setText(position == 0 ? "Em andamento" : "Finalizado")
        ).attach();

        return v;
    }
}