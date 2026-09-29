package br.com.arthurbaby.network.dto;

public class FavoritoRequest {
    public Long clienteId;
    public Long produtoId;

    public FavoritoRequest(Long clienteId, Long produtoId) {
        this.clienteId = clienteId;
        this.produtoId = produtoId;
    }
}