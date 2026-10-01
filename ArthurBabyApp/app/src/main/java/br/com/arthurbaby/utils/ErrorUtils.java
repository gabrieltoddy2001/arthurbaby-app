package br.com.arthurbaby.utils;

import retrofit2.Response;

public class ErrorUtils {

    /**
     * Extrai a mensagem de erro do corpo da resposta.
     * Se não conseguir, retorna uma mensagem genérica baseada no código HTTP.
     */
    public static String extrairMensagem(Response<?> response) {
        if (response == null) return "Erro desconhecido";

        // Tenta ler o JSON de erro
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                if (json.contains("\"erro\"")) {
                    int inicio = json.indexOf("\"erro\"") + 8;
                    int fim = json.indexOf("\"", inicio);
                    if (fim > inicio) {
                        return json.substring(inicio, fim);
                    }
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
            case 500: return "Erro no servidor. Tente novamente.";
            default:  return "Erro inesperado (" + code + ")";
        }
    }
}