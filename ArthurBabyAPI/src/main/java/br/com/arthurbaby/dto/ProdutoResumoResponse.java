package br.com.arthurbaby.dto;

import br.com.arthurbaby.dto.ProdutoDetalheResponse.ImagemResponse;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Produto resumido usado na listagem do catalogo e nos favoritos (sem variacoes). */
public record ProdutoResumoResponse(
        Long id, String codigo, String sku, String nome, String descricao,
        BigDecimal preco, BigDecimal precoPromocional, boolean promocao, boolean destaque, String status,
        Long categoriaId, String categoriaNome, Long marcaId, String marca,
        BigDecimal avaliacao, List<ImagemResponse> imagens) {

    /** Deve ser chamado dentro de uma transacao: le a colecao lazy de imagens. */
    public static ProdutoResumoResponse de(Produto p) {
        Categoria categoria = p.getCategoria();
        Marca marca = p.getMarca();
        return new ProdutoResumoResponse(
                p.getId(), p.getCodigo(), p.getSku(), p.getNome(), p.getDescricao(),
                p.getPreco(), p.getPrecoPromocional(), p.isPromocao(), p.isDestaque(),
                p.getStatus() != null ? p.getStatus().name() : null,
                categoria != null ? categoria.getId() : null, categoria != null ? categoria.getNome() : null,
                marca != null ? marca.getId() : null, marca != null ? marca.getNome() : null,
                p.getAvaliacao(),
                p.getImagens().stream()
                        .sorted(Comparator.comparingInt(ProdutoImagem::getOrdemExibicao))
                        .map(ImagemResponse::de).toList());
    }
}
