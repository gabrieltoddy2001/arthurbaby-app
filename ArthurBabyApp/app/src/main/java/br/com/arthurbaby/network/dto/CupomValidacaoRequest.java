package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;

public class CupomValidacaoRequest {
    public String codigo;
    public BigDecimal subtotal;

    public CupomValidacaoRequest(String codigo, BigDecimal subtotal) {
        this.codigo = codigo;
        this.subtotal = subtotal;
    }
}