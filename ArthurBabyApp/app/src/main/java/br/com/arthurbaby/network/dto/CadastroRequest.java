package br.com.arthurbaby.network.dto;

public class CadastroRequest {
    public String nomeCompleto;
    public String email;
    public String cpf;
    public String telefone;
    public String senha;
    public boolean aceiteTermoUso;
    public boolean aceiteLgpd;
    public EnderecoRequest endereco;

    public CadastroRequest(String nomeCompleto, String email, String cpf,
                           String telefone, String senha,
                           boolean aceiteTermoUso, boolean aceiteLgpd,
                           EnderecoRequest endereco) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.senha = senha;
        this.aceiteTermoUso = aceiteTermoUso;
        this.aceiteLgpd = aceiteLgpd;
        this.endereco = endereco;
    }
}