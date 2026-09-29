package br.com.arthurbaby.dto;
import java.math.BigDecimal;
public record CupomValidacaoRequest(String codigo, BigDecimal subtotal) {}
