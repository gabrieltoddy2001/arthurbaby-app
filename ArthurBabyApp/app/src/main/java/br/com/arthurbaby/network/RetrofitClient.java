package br.com.arthurbaby.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import br.com.arthurbaby.utils.SessionExpiredHandler;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /**
     * URL do backend.
     * - 10.0.2.2 = localhost do PC, visto de dentro do emulador Android
     * - localhost = quando usa adb reverse
     */
    private static final String BASE_URL = "http://192.168.100.171:8080/";

    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {

            // Log completo
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor que detecta 401 e força logout
            okhttp3.Interceptor authCheck = chain -> {
                okhttp3.Response response = chain.proceed(chain.request());
                if (response.code() == 401) {
                    SessionExpiredHandler.tratar(context.getApplicationContext());
                }
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(authCheck)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApi(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}