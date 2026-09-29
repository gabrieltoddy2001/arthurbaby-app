package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.MovimentacaoEstoque;
import java.time.LocalDateTime;

/** Movimentacao de estoque sem expor dados do usuario (apenas o id de quem registrou). */
public record MovimentacaoEstoqueResponse(
        Long id, Long produtoId, Long variacaoId, String sku, String tipo, int quantidade,
        Integer estoqueAnterior, Integer estoquePosterior, String observacao, Long usuarioId, LocalDateTime data) {

    public static MovimentacaoEstoqueResponse de(MovimentacaoEstoque m) {
        return new MovimentacaoEstoqueResponse(
                m.getId(), m.getProduto().getId(),
                m.getVariacao() != null ? m.getVariacao().getId() : null,
                m.getVariacao() != null ? m.getVariacao().getSku() : null,
                m.getTipo() != null ? m.getTipo().name() : null, m.getQuantidade(),
                m.getEstoqueAnterior(), m.getEstoquePosterior(), m.getObservacao(),
                m.getUsuario() != null ? m.getUsuario().getId() : null, m.getCriadoEm());
    }
}
