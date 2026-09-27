package br.com.arthurbaby.network;

import android.content.Context;
import android.os.Build;

import java.util.concurrent.TimeUnit;

import br.com.arthurbaby.BuildConfig;
import br.com.arthurbaby.utils.SessionExpiredHandler;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /**
     * URL do backend, gerada pelo Gradle (app/build.gradle.kts):
     * - emulador: 10.0.2.2 = localhost do PC, visto de dentro do emulador Android
     * - celular físico: IP do PC na rede local, detectado a cada build
     *   (pode ser fixado com api.host=... em local.properties)
     */
    private static final String BASE_URL = isEmulador()
            ? BuildConfig.API_URL_EMULATOR
            : BuildConfig.API_URL_DEVICE;

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

    private static boolean isEmulador() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.contains("emulator")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk");
    }

    public static ApiService getApi(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}