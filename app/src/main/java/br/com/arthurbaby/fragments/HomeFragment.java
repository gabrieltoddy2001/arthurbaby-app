package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CategoriaAdapter;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.mock.MockData;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // CATEGORIAS
        RecyclerView rvCategorias = v.findViewById(R.id.rvCategorias);
        rvCategorias.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategorias.setAdapter(new CategoriaAdapter(
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

        // PRODUTOS EM DESTAQUE
        RecyclerView rvDestaques = v.findViewById(R.id.rvDestaques);
        rvDestaques.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDestaques.setAdapter(new ProdutoAdapter(
                MockData.getProdutosDestaque(),
                produto -> {
                    DetalheProdutoFragment frag = DetalheProdutoFragment.newInstance(produto);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, frag)
                            .addToBackStack(null)
                            .commit();
                }
        ));
        v.findViewById(R.id.atalhoBusca).setOnClickListener(x -> {
            // Troca para o fragment de pesquisa
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, new PesquisaFragment())
                    .addToBackStack(null)
                    .commit();
        });
        v.findViewById(R.id.btnPromocoes).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PromocoesFragment())
                        .addToBackStack(null)
                        .commit()
        );
        return v;
    }
}