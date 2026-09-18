package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.mock.MockData;

public class ProdutosCategoriaFragment extends Fragment {

    private static final String ARG_CATEGORIA = "categoria";
    private String categoria;

    public static ProdutosCategoriaFragment newInstance(String categoria) {
        ProdutosCategoriaFragment f = new ProdutosCategoriaFragment();
        Bundle b = new Bundle();
        b.putString(ARG_CATEGORIA, categoria);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_produtos_categoria, container, false);

        if (getArguments() != null) {
            categoria = getArguments().getString(ARG_CATEGORIA);
        }

        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        tvTitulo.setText(categoria);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        RecyclerView rvProdutos = v.findViewById(R.id.rvProdutos);
        rvProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProdutos.setAdapter(new ProdutoAdapter(
                MockData.getProdutosPorCategoria(categoria),
                produto -> {
                    DetalheProdutoFragment frag = DetalheProdutoFragment.newInstance(produto);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, frag)
                            .addToBackStack(null)
                            .commit();
                }
        ));

        return v;
    }
}