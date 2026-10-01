package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;

public class CupomValidacaoResponse {
    public boolean valido;
    public String codigo;
    public String tipo;
    public String descricao;
    public BigDecimal desconto;
    public boolean freteGratis;
    public String motivo;
}