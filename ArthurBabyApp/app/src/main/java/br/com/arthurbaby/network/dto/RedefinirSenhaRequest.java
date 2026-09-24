package br.com.arthurbaby.network.dto;

public class RedefinirSenhaRequest {
    public String token;
    public String novaSenha;

    public RedefinirSenhaRequest(String token, String novaSenha) {
        this.token = token;
        this.novaSenha = novaSenha;
    }
}