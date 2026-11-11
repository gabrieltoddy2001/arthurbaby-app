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
     * - Emulador: sempre http://10.0.2.2:8080/ (o localhost do PC visto de dentro do emulador)
     * - Celular físico: "api.url" do local.properties (IP do PC na mesma rede Wi-Fi).
     *   Antes o IP ficava fixo no código e, quando o IP do PC mudava, o login dava timeout.
     */
    static final String URL_EMULADOR = "http://192.168.100.171:8080/";
    private static final String BASE_URL = ehEmulador() ? URL_EMULADOR : br.com.arthurbaby.BuildConfig.API_URL;

    private static boolean ehEmulador() {
        String fp = android.os.Build.FINGERPRINT;
        String hw = android.os.Build.HARDWARE;
        String produto = android.os.Build.PRODUCT;
        return (fp != null && (fp.startsWith("generic") || fp.contains("emulator")))
                || "goldfish".equals(hw) || "ranchu".equals(hw)
                || (produto != null && (produto.contains("sdk") || produto.contains("emulator")));
    }

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
