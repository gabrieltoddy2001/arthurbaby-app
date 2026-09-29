package br.com.arthurbaby.network.dto;

public class PedidoStatusHistoricoResponse {
    public Long id;
    public String statusAnterior;
    public String statusNovo;
    public String observacao;
    public Long usuarioId;
    public String data;              // ← era Date
}