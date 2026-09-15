package br.com.arthurbaby.dto;
public record CadastroRequest(String nomeCompleto, String email, String cpf, String telefone, String senha,
                              boolean aceiteTermoUso, boolean aceiteLgpd) {}
