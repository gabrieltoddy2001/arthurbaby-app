package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CategoriaAdapter;
import br.com.arthurbaby.mock.MockData;

public class CategoriasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_categorias, container, false);

        // Atalho de busca no topo
        v.findViewById(R.id.atalhoBuscaCat).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        RecyclerView rv = v.findViewById(R.id.rvCategorias);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new CategoriaAdapter(
                MockData.getCategorias(),
                categoria -> {
                    ProdutosCategoriaFragment frag = ProdutosCategoriaFragment.newInstance(categoria.getNome());
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