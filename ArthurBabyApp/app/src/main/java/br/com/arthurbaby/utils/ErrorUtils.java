package br.com.arthurbaby.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Response;

public class ErrorUtils {

    private static final String TAG = "ArthurBabyApi";

    /**
     * Extrai a mensagem de erro do corpo da resposta ({"erro": "..."} ou {"mensagem": "..."}).
     * Se não conseguir, retorna uma mensagem genérica baseada no código HTTP.
     */
    public static String extrairMensagem(Response<?> response) {
        if (response == null) return "Erro desconhecido";

        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                if (!json.isEmpty()) {
                    JSONObject obj = new JSONObject(json);
                    String msg = obj.optString("erro", obj.optString("mensagem", ""));
                    if (!msg.isEmpty()) return msg;
                }
            }
        } catch (Exception ignored) {}

        // Fallback por código HTTP
        int code = response.code();
        switch (code) {
            case 400: return "Dados inválidos";
            case 401: return "Sessão expirada. Faça login novamente.";
            case 403: return "Você não tem permissão para isso.";
            case 404: return "Recurso não encontrado";
            case 409: return "Registro duplicado ou em uso.";
            case 500: return "Erro no servidor. Tente novamente.";
            default:  return "Erro inesperado (" + code + ")";
        }
    }

    /**
     * Mensagem amigável para falhas de chamada (onFailure do Retrofit).
     * Antes o app exibia "Erro de conexão: java.lang.NumberFormatException..." —
     * erro de conversão do JSON não é problema de conexão e não deve aparecer cru ao usuário.
     * O detalhe técnico vai para o Logcat (tag ArthurBabyApi).
     */
    public static String mensagemFalha(Throwable t) {
        Log.e(TAG, "Falha na chamada à API", t);
        if (t instanceof SocketTimeoutException) {
            return "O servidor demorou para responder. Tente novamente.";
        }
        if (t instanceof ConnectException || t instanceof UnknownHostException) {
            return "Não foi possível conectar ao servidor. Verifique sua internet.";
        }
        if (t instanceof IOException) {
            return "Falha de conexão. Verifique sua internet e tente novamente.";
        }
        // JsonSyntaxException, NumberFormatException, IllegalStateException...: resposta fora do esperado
        return "Não foi possível carregar os dados. Tente novamente mais tarde.";
    }
}
