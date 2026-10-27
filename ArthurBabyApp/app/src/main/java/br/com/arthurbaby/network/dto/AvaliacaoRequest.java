package br.com.arthurbaby.network.dto;

public class AvaliacaoRequest {
    public Long clienteId;
    public int nota;
    public String comentario;

    public AvaliacaoRequest(Long clienteId, int nota, String comentario) {
        this.clienteId = clienteId;
        this.nota = nota;
        this.comentario = comentario;
    }
}