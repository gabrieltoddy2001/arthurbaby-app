package br.com.arthurbaby.repositories;

import android.content.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.FavoritoRequest;
import br.com.arthurbaby.network.dto.FavoritoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Favoritos com cache local de IDs.
 *
 * - isFavorito() consulta o cache (síncrono, sem internet)
 * - toggle() chama a API e atualiza o cache
 * - sincronizar() carrega tudo do backend ao entrar no app
 */
public class FavoritoRepository {

    private static FavoritoRepository instance;
    private final Set<Long> idsFavoritos = new HashSet<>();

    private FavoritoRepository() {}

    public static FavoritoRepository getInstance() {
        if (instance == null) instance = new FavoritoRepository();
        return instance;
    }

    public boolean isFavorito(Long produtoId) {
        return idsFavoritos.contains(produtoId);
    }

    /**
     * Sincroniza o cache com o backend.
     * Chame quando o app inicia, ou após login.
     */
    public void sincronizar(Context ctx) {
        Long clienteId = TokenStorage.getUsuarioId(ctx);
        if (clienteId == null) return;

        ApiService api = RetrofitClient.getApi(ctx);
        api.listarFavoritos(clienteId).enqueue(new Callback<List<FavoritoResponse>>() {
            @Override
            public void onResponse(Call<List<FavoritoResponse>> call,
                                   Response<List<FavoritoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    idsFavoritos.clear();
                    for (FavoritoResponse f : response.body()) {
                        if (f.produto != null && f.produto.id != null) {
                            idsFavoritos.add(f.produto.id);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoResponse>> call, Throwable t) {
                // mantém o cache atual
            }
        });
    }

    /**
     * Alterna favorito. Atualiza o cache local imediatamente e chama a API em background.
     * @return novo estado (true = favoritado, false = não favoritado)
     */
    public boolean toggle(Context ctx, Long produtoId, Runnable onUpdate) {
        Long clienteId = TokenStorage.getUsuarioId(ctx);
        if (clienteId == null) return false;

        boolean agoraFavorito = !idsFavoritos.contains(produtoId);

        // Atualiza o cache local já
        if (agoraFavorito) idsFavoritos.add(produtoId);
        else idsFavoritos.remove(produtoId);

        // Chama a API em background
        ApiService api = RetrofitClient.getApi(ctx);
        FavoritoRequest req = new FavoritoRequest(clienteId, produtoId);

        if (agoraFavorito) {
            api.adicionarFavorito(req).enqueue(new Callback<FavoritoResponse>() {
                @Override public void onResponse(Call<FavoritoResponse> call, Response<FavoritoResponse> response) {
                    if (onUpdate != null) onUpdate.run();
                }
                @Override public void onFailure(Call<FavoritoResponse> call, Throwable t) {
                    // Reverte o cache em caso de falha
                    idsFavoritos.remove(produtoId);
                    if (onUpdate != null) onUpdate.run();
                }
            });
        } else {
            api.removerFavorito(req).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (onUpdate != null) onUpdate.run();
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    // Reverte
                    idsFavoritos.add(produtoId);
                    if (onUpdate != null) onUpdate.run();
                }
            });
        }

        return agoraFavorito;
    }

    public void limpar() {
        idsFavoritos.clear();
    }
}