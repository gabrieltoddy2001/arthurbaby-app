package br.com.arthurbaby.network;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        // Só adiciona o token se a rota NÃO for pública
        if (!ehPublica(original.method(), original.url().encodedPath())) {
            String token = TokenStorage.getToken(context);
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        return chain.proceed(builder.build());
    }

    /**
     * Espelha as regras permitAll do SecurityConfig da API.
     * Antes qualquer caminho contendo "/api/produtos" era tratado como público — inclusive
     * POST /api/produtos/{id}/avaliacoes, que exige login: a avaliação ia sem token,
     * voltava 401 e o app deslogava o usuário.
     */
    static boolean ehPublica(String metodo, String path) {
        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/cadastro")
                || path.startsWith("/api/auth/recuperar-senha")
                || path.startsWith("/api/auth/redefinir-senha")) {
            return true;
        }
        if ("POST".equals(metodo) && path.equals("/api/cupons/validar")) return true;
        if ("GET".equals(metodo)) {
            return path.equals("/api/cupons/ativos")
                    || path.startsWith("/api/categorias")
                    || path.startsWith("/api/produtos")
                    || path.startsWith("/api/estoque");
        }
        return false;
    }
}
