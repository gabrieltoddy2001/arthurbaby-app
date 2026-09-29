package br.com.arthurbaby.dto;
import java.math.BigDecimal;
/** {@code motivo} vem preenchido quando {@code valido} e false. */
public record CupomValidacaoResponse(boolean valido, String codigo, String tipo, String descricao,
                                     BigDecimal desconto, boolean freteGratis, String motivo) {}
