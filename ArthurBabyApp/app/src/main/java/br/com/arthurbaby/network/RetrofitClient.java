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
     * URL do backend: por padrão a API no Render; para usar a API do PC, defina "api.url"
     * no local.properties (10.0.2.2 no emulador, IP do PC no celular). Veja o README.
     */
    private static final String BASE_URL = br.com.arthurbaby.BuildConfig.API_URL;

    public static String getBaseUrl() { return BASE_URL; }

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
            // O próprio "Sair" pode receber 401 (token já vencido): a pessoa já está saindo, não é "sessão expirada"
            if (response.code() == 401 && request.header("Authorization") != null
                    && !request.url().encodedPath().equals("/api/auth/logout")) {
                SessionExpiredHandler.tratar(appContext);
            }
            return response;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(appContext))
                .addInterceptor(authCheck)
                .addInterceptor(logging)
                // O servidor no Render "dorme" sem uso e leva até ~1 min para responder a primeira chamada
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(70, TimeUnit.SECONDS)
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