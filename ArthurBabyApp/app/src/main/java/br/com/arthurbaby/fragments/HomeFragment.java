package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CategoriaIconeAdapter;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.mock.MockData;
import br.com.arthurbaby.repositories.ProdutoRepository;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // ATALHO DE BUSCA
        v.findViewById(R.id.atalhoBusca).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        // BOTÃO FAVORITOS NO TOPO
        v.findViewById(R.id.btnFavoritos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new FavoritosFragment())
                        .addToBackStack(null)
                        .commit());

        // BANNER
        v.findViewById(R.id.btnBanner).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PromocoesFragment())
                        .addToBackStack(null)
                        .commit());

        // "VER TODAS" das categorias
        v.findViewById(R.id.txtVerTodasCategorias).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new CategoriasFragment())
                        .addToBackStack(null)
                        .commit());

        // "VER TODOS" dos produtos
        v.findViewById(R.id.txtVerTodosProdutos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        // CATEGORIAS — grid horizontal com ícones
        RecyclerView rvCategorias = v.findViewById(R.id.rvCategorias);
        rvCategorias.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategorias.setAdapter(new CategoriaIconeAdapter(
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

        // PRODUTOS EM DESTAQUE — 2 colunas, últimos 4 produtos
        RecyclerView rvDestaques = v.findViewById(R.id.rvDestaques);
        rvDestaques.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvDestaques.setAdapter(new ProdutoAdapter(
                ProdutoRepository.getInstance().getTodos().subList(0, 4),
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