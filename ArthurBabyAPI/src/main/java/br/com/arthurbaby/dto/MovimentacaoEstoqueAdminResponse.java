package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.MovimentacaoEstoque;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Movimentacao de estoque para o painel admin: inclui nome do produto e de quem registrou. */
@Schema(description = "Movimentação de estoque (visão do painel administrativo)")
public record MovimentacaoEstoqueAdminResponse(
        @Schema(description = "Id da movimentação", example = "45") Long id,
        @Schema(description = "Id do produto", example = "1") Long produtoId,
        @Schema(description = "Nome do produto", example = "Body Bebê Manga Curta") String produtoNome,
        @Schema(description = "Id da variação (nulo em movimentações do produto inteiro)", example = "1") Long variacaoId,
        @Schema(description = "SKU da variação", example = "AB-001-RN-BR") String sku,
        @Schema(description = "Tipo da movimentação", example = "ENTRADA", allowableValues = {"ENTRADA", "SAIDA", "AJUSTE", "RESERVA", "ESTORNO"}) String tipo,
        @Schema(description = "Unidades movimentadas (no ajuste, a diferença absoluta)", example = "20") int quantidade,
        @Schema(description = "Estoque da variação antes da operação", example = "10") Integer estoqueAnterior,
        @Schema(description = "Estoque da variação depois da operação", example = "30") Integer estoquePosterior,
        @Schema(description = "Observação registrada", example = "Recebimento NF 1234 - fornecedor Baby Têxtil") String observacao,
        @Schema(description = "Id do usuário que registrou", example = "1") Long usuarioId,
        @Schema(description = "Nome do usuário que registrou", example = "Administrador ArthurBaby") String usuario,
        @Schema(description = "Data/hora da movimentação", example = "2026-09-28T10:15:00") LocalDateTime data) {

    /** Converte a entidade; variação e usuário podem ser nulos em registros antigos. */
    public static MovimentacaoEstoqueAdminResponse de(MovimentacaoEstoque m) {
        return new MovimentacaoEstoqueAdminResponse(
                m.getId(), m.getProduto().getId(), m.getProduto().getNome(),
                m.getVariacao() != null ? m.getVariacao().getId() : null,
                m.getVariacao() != null ? m.getVariacao().getSku() : null,
                m.getTipo() != null ? m.getTipo().name() : null, m.getQuantidade(),
                m.getEstoqueAnterior(), m.getEstoquePosterior(), m.getObservacao(),
                m.getUsuario() != null ? m.getUsuario().getId() : null,
                m.getUsuario() != null ? m.getUsuario().getNomeCompleto() : null, m.getCriadoEm());
    }
}
