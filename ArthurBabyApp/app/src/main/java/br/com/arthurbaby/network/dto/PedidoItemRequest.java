package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;

public class PedidoItemRequest {
    public Long produtoId;
    public Long variacaoId;
    public Integer quantidade;
    public BigDecimal desconto;

    public PedidoItemRequest(Long produtoId, Long variacaoId, Integer quantidade) {
        this.produtoId = produtoId;
        this.variacaoId = variacaoId;
        this.quantidade = quantidade;
        this.desconto = BigDecimal.ZERO;
    }
}