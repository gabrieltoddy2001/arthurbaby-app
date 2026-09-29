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
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.PageResponse;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromocoesFragment extends Fragment {

    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_promocoes, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rv = v.findViewById(R.id.rvPromocoes);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        carregarPromocoes();
        return v;
    }

    private void carregarPromocoes() {
        ApiService api = RetrofitClient.getApi(requireContext());
        // Filtro promocao=true
        api.listarProdutos(null, null, true, 0, 50)
                .enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {
                            rv.setAdapter(new ProdutoAdapter(
                                    Conversor.paraProdutos(response.body().content),
                                    produto -> {
                                        DetalheProdutoFragment frag =
                                                DetalheProdutoFragment.newInstance(produto);
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.frameContainer, frag)
                                                .addToBackStack(null)
                                                .commit();
                                    }));
                        } else {
                            Toast.makeText(requireContext(),
                                    "Nenhuma promoção encontrada", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ProdutoResponse>> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}