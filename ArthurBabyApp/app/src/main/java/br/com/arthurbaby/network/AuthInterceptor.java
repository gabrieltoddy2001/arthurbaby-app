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
        String token = TokenStorage.getToken(context);

        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}