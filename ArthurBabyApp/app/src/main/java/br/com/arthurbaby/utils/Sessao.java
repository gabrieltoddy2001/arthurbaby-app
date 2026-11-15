package br.com.arthurbaby.utils;

import android.content.Context;

import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.repositories.FavoritoRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Encerramento da sessão do cliente ("Sair"). */
public final class Sessao {

    private Sessao() {}

    /**
     * Avisa a API (POST /api/auth/logout invalida o token no servidor, em qualquer aparelho) e
     * limpa a sessão local na hora, sem esperar a resposta: sem internet a pessoa sai do mesmo jeito.
     * O token vai explícito no cabeçalho porque a sessão local é apagada antes de a requisição sair.
     */
    public static void sair(Context ctx) {
        Context app = ctx.getApplicationContext();
        String token = TokenStorage.getToken(app);
        if (token != null && !token.isEmpty()) {
            RetrofitClient.getApi(app).logout("Bearer " + token).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) { }
                @Override public void onFailure(Call<Void> call, Throwable t) { }
            });
        }
        TokenStorage.limpar(app);
        FavoritoRepository.getInstance().limpar();
    }
}
