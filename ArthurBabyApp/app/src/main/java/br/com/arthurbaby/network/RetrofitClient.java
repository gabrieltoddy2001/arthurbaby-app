package br.com.arthurbaby.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;

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
     * - IP da rede local = celular físico na mesma rede Wi-Fi do PC
     */
    private static final String BASE_URL = "http://192.168.100.171:8080/";

    private static volatile Retrofit retrofit;
    private static volatile ApiService api;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) retrofit = criar(context.getApplicationContext());
            }
        }
        return retrofit;
    }

    private static Retrofit criar(Context appContext) {
        // Log do corpo apenas em build de debug: em release ele exporia token JWT e senhas no Logcat
        boolean debug = (appContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(debug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        logging.redactHeader("Authorization");

        // Interceptor que detecta 401 (token expirado/inválido) e força logout.
        // Só quando a requisição levava token: 401 sem token não significa sessão expirada.
        okhttp3.Interceptor authCheck = chain -> {
            okhttp3.Request request = chain.request();
            okhttp3.Response response = chain.proceed(request);
            if (response.code() == 401 && request.header("Authorization") != null) {
                SessionExpiredHandler.tratar(appContext);
            }
            return response;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(appContext))
                .addInterceptor(authCheck)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiService getApi(Context context) {
        if (api == null) {
            synchronized (RetrofitClient.class) {
                if (api == null) api = getInstance(context).create(ApiService.class);
            }
        }
        return api;
    }
}
