package br.com.arthurbaby.network.dto;

public class EnderecoRequest {
    public String cep;
    public String logradouro;
    public String numero;
    public String complemento;
    public String bairro;
    public String cidade;
    public String uf;
    public String referencia;
    public boolean principal;

    public EnderecoRequest(String cep, String logradouro, String numero,
                           String complemento, String bairro, String cidade,
                           String uf, String referencia, boolean principal) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.referencia = referencia;
        this.principal = principal;
    }
}