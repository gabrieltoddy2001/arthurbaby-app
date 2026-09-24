package br.com.arthurbaby.dto;

public record EnderecoRequest(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String referencia,
        Boolean principal
) {}
