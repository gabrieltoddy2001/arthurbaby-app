package br.com.arthurbaby.network.dto;

public class AuthRequest {
    public String login;
    public String senha;

    public AuthRequest(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }
}