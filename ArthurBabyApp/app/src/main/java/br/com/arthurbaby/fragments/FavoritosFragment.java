package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.FavoritoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosFragment extends Fragment {

    private RecyclerView rv;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_favoritos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rv = v.findViewById(R.id.rvFavoritos);
        tvVazio = v.findViewById(R.id.tvVazio);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        carregarFavoritos();
        return v;
    }

    private void carregarFavoritos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) {
            tvVazio.setText("Faça login para ver favoritos");
            tvVazio.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarFavoritos(clienteId).enqueue(new Callback<List<FavoritoResponse>>() {
            @Override
            public void onResponse(Call<List<FavoritoResponse>> call,
                                   Response<List<FavoritoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Produto> produtos = new ArrayList<>();
                    for (FavoritoResponse f : response.body()) {
                        if (f.produto != null) {
                            produtos.add(Conversor.paraProduto(f.produto));
                        }
                    }

                    if (produtos.isEmpty()) {
                        tvVazio.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        tvVazio.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        rv.setAdapter(new ProdutoAdapter(produtos, produto -> {
                            DetalheProdutoFragment frag =
                                    DetalheProdutoFragment.newInstance(produto);
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frameContainer, frag)
                                    .addToBackStack(null)
                                    .commit();
                        }));
                    }
                } else {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar favoritos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoResponse>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}