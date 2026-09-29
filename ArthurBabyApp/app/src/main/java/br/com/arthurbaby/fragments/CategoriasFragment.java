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

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CategoriaAdapter;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.CategoriaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriasFragment extends Fragment {

    private RecyclerView rv;
    private CategoriaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_categorias, container, false);

        v.findViewById(R.id.atalhoBuscaCat).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        rv = v.findViewById(R.id.rvCategorias);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarCategorias();
        return v;
    }

    private void carregarCategorias() {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarCategorias().enqueue(new Callback<List<CategoriaResponse>>() {
            @Override
            public void onResponse(Call<List<CategoriaResponse>> call,
                                   Response<List<CategoriaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new CategoriaAdapter(
                            Conversor.paraCategorias(response.body()),
                            categoria -> {
                                ProdutosCategoriaFragment frag = ProdutosCategoriaFragment.newInstance(
                                        categoria.getId(), categoria.getNome());
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameContainer, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaResponse>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}