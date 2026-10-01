package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;

public class PedidoItemResponse {
    public Long produtoId;
    public Long variacaoId;
    public String codigoProduto;
    public String nomeProduto;
    public String variacaoDescricao;
    public int quantidade;
    public BigDecimal valorUnitario;
    public BigDecimal valorTotal;
}