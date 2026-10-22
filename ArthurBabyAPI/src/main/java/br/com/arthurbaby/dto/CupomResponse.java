package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Cupom;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cupom vigente exibido na tela "Meus Cupons" do app.
 * {@code validoAte} sai sem milissegundos (ex.: 2026-12-31T23:59:59), como combinado com o app;
 * o {@link JsonFormat} do campo tem prioridade sobre o formato global do JacksonConfig.
 */
@Schema(description = "Cupom ativo e dentro da validade")
public record CupomResponse(
        @Schema(description = "Código para digitar no carrinho", example = "ARTHUR10") String codigo,
        @Schema(description = "Descrição do cupom", example = "10% de desconto em todo o site") String descricao,
        @Schema(description = "Tipo do desconto", example = "PERCENTUAL", allowableValues = {"PERCENTUAL", "VALOR_FIXO", "FRETE_GRATIS"}) String tipo,
        @Schema(description = "Percentual (PERCENTUAL) ou valor em reais (VALOR_FIXO); 0 para FRETE_GRATIS", example = "10.0") BigDecimal valor,
        @Schema(description = "Fim da validade; nulo = não expira por data", nullable = true, example = "2026-12-31T23:59:59")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime validoAte) {

    public static CupomResponse de(Cupom c) {
        // Cupom sem valor (ex.: FRETE_GRATIS) sai como 0 em vez de null para o app nao precisar tratar nulo
        BigDecimal valor = c.getValor() != null ? c.getValor() : BigDecimal.ZERO;
        return new CupomResponse(c.getCodigo(), c.getDescricao(), c.getTipo().name(), valor, c.getValidoAte());
    }
}
