package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.PedidoAdapter;
import br.com.arthurbaby.models.Pedido;

public class ListaPedidosFragment extends Fragment {

    private static final String ARG_PEDIDOS = "pedidos";

    public static ListaPedidosFragment newInstance(List<Pedido> pedidos) {
        ListaPedidosFragment f = new ListaPedidosFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_PEDIDOS, (Serializable) pedidos);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_lista_pedidos, container, false);

        RecyclerView rv = v.findViewById(R.id.rvPedidos);
        TextView tvVazio = v.findViewById(R.id.tvVazio);

        List<Pedido> pedidos = null;
        if (getArguments() != null) {
            pedidos = (List<Pedido>) getArguments().getSerializable(ARG_PEDIDOS);
        }

        if (pedidos == null || pedidos.isEmpty()) {
            tvVazio.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvVazio.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new PedidoAdapter(pedidos, pedido -> {
                DetalhePedidoFragment frag = DetalhePedidoFragment.newInstance(pedido);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, frag)
                        .addToBackStack(null)
                        .commit();
            }));
        }

        return v;
    }
}