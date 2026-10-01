package br.com.arthurbaby.network;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();

        // Rotas públicas — não precisam de token
        boolean publica = path.contains("/api/auth/login")
                || path.contains("/api/auth/cadastro")
                || path.contains("/api/auth/recuperar-senha")
                || path.contains("/api/auth/redefinir-senha")
                || path.contains("/api/categorias")
                || path.contains("/api/produtos")
                || path.contains("/api/cupons/validar");

        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        // Só adiciona o token se a rota NÃO for pública
        if (!publica) {
            String token = TokenStorage.getToken(context);
            if (token != null && !token.isEmpty()) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        return chain.proceed(builder.build());
    }
}