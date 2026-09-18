package br.com.arthurbaby.dto;
import java.math.BigDecimal;
public record PedidoItemRequest(Long produtoId, Long variacaoId, Integer quantidade, BigDecimal desconto) {}
