package br.com.arthurbaby.dto;
/** {@code quantidade}: unidades para entrada/estorno, ou o novo estoque total no ajuste. */
public record EstoqueOperacaoRequest(Integer quantidade, String observacao) {}
