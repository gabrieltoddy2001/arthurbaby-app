package br.com.arthurbaby.network.dto;

public class ClienteRequest {
    public String nomeCompleto;
    public String email;
    public String cpf;
    public String telefone;
    public String senha;

    public ClienteRequest(String nomeCompleto, String email,
                          String cpf, String telefone) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
    }
}