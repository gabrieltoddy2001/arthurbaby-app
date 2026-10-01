package br.com.arthurbaby.models;

import java.io.Serializable;

public class Endereco implements Serializable {
    private Long id;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String referencia;
    private boolean principal;

    public Endereco(Long id, String cep, String logradouro, String numero,
                    String complemento, String bairro, String cidade,
                    String uf, String referencia, boolean principal) {
        this.id = id;
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

    public Long getId() { return id; }
    public String getCep() { return cep; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getReferencia() { return referencia; }
    public boolean isPrincipal() { return principal; }

    public String resumo() {
        return logradouro + ", " + numero +
                (complemento != null && !complemento.isEmpty() ? " - " + complemento : "") +
                "\n" + bairro + " - " + cidade + "/" + uf + "\nCEP: " + cep;
    }
}