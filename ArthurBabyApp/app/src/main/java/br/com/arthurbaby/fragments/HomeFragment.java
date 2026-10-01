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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CategoriaIconeAdapter;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.CategoriaResponse;
import br.com.arthurbaby.network.dto.PageResponse;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.utils.AuthGuard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private CategoriaIconeAdapter categoriaAdapter;
    private ProdutoAdapter produtoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // === LOADING ===
        View progressBar = v.findViewById(R.id.progressBar);
        View scrollConteudo = v.findViewById(R.id.scrollConteudo);
        br.com.arthurbaby.utils.LoadingView.mostrar(progressBar, scrollConteudo);

// === CABEÇALHO ===
        TextView btnEntrar = v.findViewById(R.id.btnEntrarHome);
        View btnFav = v.findViewById(R.id.btnFavoritos);
        TextView tvSaudacao = v.findViewById(R.id.tvSaudacao);

        if (br.com.arthurbaby.utils.AuthGuard.estaLogado(requireContext())) {
            // Logada: mostra nome + coração
            String nome = br.com.arthurbaby.network.TokenStorage.getNome(requireContext());
            if (nome != null && !nome.isEmpty()) {
                String primeiroNome = nome.split(" ")[0];
                tvSaudacao.setText("Olá, " + primeiroNome + "! 💕");
            } else {
                tvSaudacao.setText("Olá! 💕");
            }
            btnEntrar.setVisibility(View.GONE);
            btnFav.setVisibility(View.VISIBLE);
        } else {
            // Visitante: saudação genérica + botão Entrar
            tvSaudacao.setText("Olá! 💕");
            btnEntrar.setVisibility(View.VISIBLE);
            btnFav.setVisibility(View.GONE);
        }

        btnEntrar.setOnClickListener(x ->
                br.com.arthurbaby.utils.AuthGuard.mostrarDialogLogin(requireActivity(),
                        "Entre para aproveitar todas as vantagens da ArthurBaby!"));

        btnFav.setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new FavoritosFragment())
                        .addToBackStack(null)
                        .commit());

        // === ATALHO DE BUSCA ===
        v.findViewById(R.id.atalhoBusca).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        // === BANNER ===
        v.findViewById(R.id.btnBanner).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PromocoesFragment())
                        .addToBackStack(null)
                        .commit());

        // === "VER TODAS" CATEGORIAS ===
        v.findViewById(R.id.txtVerTodasCategorias).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new CategoriasFragment())
                        .addToBackStack(null)
                        .commit());

        // === "VER TODOS" PRODUTOS ===
        v.findViewById(R.id.txtVerTodosProdutos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new PesquisaFragment())
                        .addToBackStack(null)
                        .commit());

        // === CATEGORIAS ===
        RecyclerView rvCategorias = v.findViewById(R.id.rvCategorias);
        rvCategorias.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriaAdapter = new CategoriaIconeAdapter(
                new java.util.ArrayList<>(),
                categoria -> {
                    ProdutosCategoriaFragment frag = ProdutosCategoriaFragment.newInstance(
                            categoria.getId(), categoria.getNome());
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, frag)
                            .addToBackStack(null)
                            .commit();
                });
        rvCategorias.setAdapter(categoriaAdapter);

        // === PRODUTOS EM DESTAQUE ===
        RecyclerView rvDestaques = v.findViewById(R.id.rvDestaques);
        rvDestaques.setLayoutManager(new GridLayoutManager(getContext(), 2));
        produtoAdapter = new ProdutoAdapter(
                new java.util.ArrayList<>(),
                produto -> {
                    DetalheProdutoFragment frag = DetalheProdutoFragment.newInstance(produto);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, frag)
                            .addToBackStack(null)
                            .commit();
                });
        rvDestaques.setAdapter(produtoAdapter);

        // Carrega com callback para esconder loading
        carregarComLoading(progressBar, scrollConteudo);

        return v;
    }

    private void carregarComLoading(View progressBar, View scrollConteudo) {
        ApiService api = RetrofitClient.getApi(requireContext());

        // Contador de chamadas concluídas (2 = ambas terminaram)
        final int[] concluidas = {0};

        Runnable onFinish = () -> {
            concluidas[0]++;
            if (concluidas[0] >= 2) {
                br.com.arthurbaby.utils.LoadingView.esconder(progressBar, scrollConteudo);
            }
        };

        // Categorias
        api.listarCategorias().enqueue(new Callback<List<CategoriaResponse>>() {
            @Override
            public void onResponse(Call<List<CategoriaResponse>> call,
                                   Response<List<CategoriaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriaAdapter = new CategoriaIconeAdapter(
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
                    if (getView() != null) {
                        RecyclerView rv = getView().findViewById(R.id.rvCategorias);
                        rv.setAdapter(categoriaAdapter);
                    }
                }
                onFinish.run();
            }

            @Override
            public void onFailure(Call<List<CategoriaResponse>> call, Throwable t) {
                onFinish.run();
            }
        });

        // Produtos
        api.listarProdutos(null, null, null, 0, 20)
                .enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {
                            List<Produto> produtos = Conversor.paraProdutos(response.body().content);
                            produtoAdapter = new ProdutoAdapter(produtos, produto -> {
                                DetalheProdutoFragment frag =
                                        DetalheProdutoFragment.newInstance(produto);
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameContainer, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            if (getView() != null) {
                                RecyclerView rv = getView().findViewById(R.id.rvDestaques);
                                rv.setAdapter(produtoAdapter);
                            }
                        }
                        onFinish.run();
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ProdutoResponse>> call, Throwable t) {
                        onFinish.run();
                    }
                });
    }

    private void carregarCategorias() {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarCategorias().enqueue(new Callback<List<CategoriaResponse>>() {
            @Override
            public void onResponse(Call<List<CategoriaResponse>> call,
                                   Response<List<CategoriaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriaAdapter = new CategoriaIconeAdapter(
                            Conversor.paraCategorias(response.body()),
                            categoria -> {
                                ProdutosCategoriaFragment frag =
                                        ProdutosCategoriaFragment.newInstance(
                                                categoria.getId(), categoria.getNome());
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameContainer, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                    RecyclerView rv = requireView().findViewById(R.id.rvCategorias);
                    rv.setAdapter(categoriaAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaResponse>> call, Throwable t) {
                // silencioso
            }
        });
    }

    private void carregarProdutos() {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarProdutos(null, null, null, 0, 20)
                .enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {
                            List<Produto> produtos = Conversor.paraProdutos(response.body().content);
                            produtoAdapter = new ProdutoAdapter(produtos, produto -> {
                                DetalheProdutoFragment frag =
                                        DetalheProdutoFragment.newInstance(produto);
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameContainer, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            RecyclerView rv = requireView().findViewById(R.id.rvDestaques);
                            rv.setAdapter(produtoAdapter);
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ProdutoResponse>> call, Throwable t) {
                        // silencioso
                    }
                });
    }
}