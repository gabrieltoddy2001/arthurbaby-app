package br.com.arthurbaby.network.dto;

import java.util.List;

public class CategoriaResponse {
    public Long id;
    public String nome;
    public String icone;
    public String descricao;
    public String imagemUrl;
    public int ordemExibicao;
    public String status;
    public List<CategoriaResponse> subcategorias;
}