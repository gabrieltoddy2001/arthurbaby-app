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

public class ProdutosCategoriaFragment extends Fragment {

    private static final String ARG_CATEGORIA_ID = "categoriaId";
    private static final String ARG_CATEGORIA_NOME = "categoriaNome";

    private Long categoriaId;
    private String categoriaNome;

    public static ProdutosCategoriaFragment newInstance(Long id, String nome) {
        ProdutosCategoriaFragment f = new ProdutosCategoriaFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_CATEGORIA_ID, id != null ? id : -1);
        b.putString(ARG_CATEGORIA_NOME, nome);
        f.setArguments(b);
        return f;
    }

    /** Compatibilidade: aceita apenas nome, usa -1 como id */
    public static ProdutosCategoriaFragment newInstance(String nome) {
        return newInstance(-1L, nome);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_produtos_categoria, container, false);

        if (getArguments() != null) {
            categoriaId = getArguments().getLong(ARG_CATEGORIA_ID, -1);
            if (categoriaId == -1) categoriaId = null;
            categoriaNome = getArguments().getString(ARG_CATEGORIA_NOME, "");
        }

        TextView tvTitulo = v.findViewById(R.id.tvTitulo);
        tvTitulo.setText(categoriaNome);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        RecyclerView rvProdutos = v.findViewById(R.id.rvProdutos);
        rvProdutos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        carregarProdutos(rvProdutos);
        return v;
    }

    private void carregarProdutos(RecyclerView rv) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarProdutos(null, categoriaId, null, 0, 50)
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
                                    "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
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