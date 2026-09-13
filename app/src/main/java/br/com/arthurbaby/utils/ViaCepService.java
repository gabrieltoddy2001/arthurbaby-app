package br.com.arthurbaby.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViaCepService {

    public interface Callback {
        void onResultado(EnderecoCep endereco);
        void onErro(String mensagem);
    }

    public static class EnderecoCep {
        public String cep;
        public String logradouro;
        public String bairro;
        public String cidade;
        public String uf;
    }

    public static void buscar(String cep, Callback callback) {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() != 8) {
            callback.onErro("CEP inválido");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("https://viacep.com.br/ws/" + cepLimpo + "/json/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) sb.append(linha);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());

                if (json.has("erro") && json.getBoolean("erro")) {
                    post(() -> callback.onErro("CEP não encontrado"));
                    return;
                }

                EnderecoCep e = new EnderecoCep();
                e.cep = json.optString("cep");
                e.logradouro = json.optString("logradouro");
                e.bairro = json.optString("bairro");
                e.cidade = json.optString("localidade");
                e.uf = json.optString("uf");

                post(() -> callback.onResultado(e));

            } catch (Exception ex) {
                post(() -> callback.onErro("Falha ao buscar CEP"));
            }
        }).start();
    }

    private static void post(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}