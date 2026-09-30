package br.com.arthurbaby.dto;

import br.com.arthurbaby.dto.ProdutoDetalheResponse.ImagemResponse;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Produto resumido usado na listagem do catalogo e nos favoritos (sem variacoes). */
@Schema(description = "Produto resumido (listagem e favoritos)")
public record ProdutoResumoResponse(
        @Schema(description = "Id do produto", example = "1") Long id,
        @Schema(description = "Código interno", example = "AB-001") String codigo,
        @Schema(description = "SKU", example = "AB-001") String sku,
        @Schema(description = "Nome", example = "Kit Enxoval Bebe") String nome,
        @Schema(description = "Descrição", example = "Kit com manta, fraldas de pano e toalha com capuz") String descricao,
        @Schema(description = "Preço de venda", example = "129.90") BigDecimal preco,
        @Schema(description = "Preço promocional", nullable = true, example = "119.90") BigDecimal precoPromocional,
        @Schema(description = "Em promoção", example = "false") boolean promocao,
        @Schema(description = "Em destaque na vitrine", example = "true") boolean destaque,
        @Schema(description = "Situação", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO", "ESGOTADO"}) String status,
        @Schema(description = "Id da categoria", example = "1") Long categoriaId,
        @Schema(description = "Nome da categoria", example = "Enxoval") String categoriaNome,
        @Schema(description = "Id da marca", example = "1") Long marcaId,
        @Schema(description = "Nome da marca", example = "ArthurBaby") String marca,
        @Schema(description = "Avaliação média (0 a 5)", example = "5.0") BigDecimal avaliacao,
        @Schema(description = "Imagens na ordem de exibição") List<ImagemResponse> imagens) {

    /** Deve ser chamado dentro de uma transacao: le a colecao lazy de imagens. */
    public static ProdutoResumoResponse de(Produto p) {
        Categoria categoria = p.getCategoria();
        Marca marca = p.getMarca();
        return new ProdutoResumoResponse(
                p.getId(), p.getCodigo(), p.getSku(), p.getNome(), p.getDescricao(),
                p.getPreco(), p.getPrecoPromocional(), p.isPromocao(), p.isDestaque(),
                p.getStatus() != null ? p.getStatus().name() : null,
                // Categoria e marca são opcionais em produtos importados do banco antigo
                categoria != null ? categoria.getId() : null, categoria != null ? categoria.getNome() : null,
                marca != null ? marca.getId() : null, marca != null ? marca.getNome() : null,
                p.getAvaliacao(),
                p.getImagens().stream()
                        .sorted(Comparator.comparingInt(ProdutoImagem::getOrdemExibicao))
                        .map(ImagemResponse::de).toList());
    }
}
