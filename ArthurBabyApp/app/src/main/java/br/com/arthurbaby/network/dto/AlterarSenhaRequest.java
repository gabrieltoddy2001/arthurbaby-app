package br.com.arthurbaby.network.dto;

public class AlterarSenhaRequest {
    public String senhaAtual;
    public String novaSenha;

    public AlterarSenhaRequest(String senhaAtual, String novaSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
    }
}