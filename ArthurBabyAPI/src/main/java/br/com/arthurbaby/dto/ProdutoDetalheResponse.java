package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.ProdutoImagem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** Detalhe completo do produto (tela de produto do app): dados, imagens e variações com estoque. */
@Schema(description = "Detalhe do produto com imagens e variações")
public record ProdutoDetalheResponse(
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
        @Schema(description = "Imagens, a principal primeiro") List<ImagemResponse> imagens,
        @Schema(description = "Variações com estoque") List<VariacaoResponse> variacoes) {

    /** Imagem do produto. */
    @Schema(description = "Imagem do produto")
    public record ImagemResponse(
            @Schema(description = "Id da imagem", example = "1") Long id,
            @Schema(description = "URL da imagem", example = "https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto") String url,
            @Schema(description = "Texto alternativo", example = "Imagem de exemplo") String descricao,
            @Schema(description = "É a imagem principal", example = "true") boolean principal) {
        public static ImagemResponse de(ProdutoImagem i) {
            return new ImagemResponse(i.getId(), i.getUrl(), i.getDescricao(), i.isPrincipal());
        }
    }

    /** Combinação vendável de tamanho/cor/modelo com o seu estoque atual. */
    @Schema(description = "Variação do produto")
    public record VariacaoResponse(
            @Schema(description = "Id da variação", example = "1") Long id,
            @Schema(description = "SKU da variação", example = "AB-001-RN-BR") String sku,
            @Schema(description = "Tamanho", example = "RN") String tamanho,
            @Schema(description = "Cor", example = "Branco") String cor,
            @Schema(description = "Modelo", example = "Padrão") String modelo,
            @Schema(description = "Preço da variação", example = "129.90") BigDecimal preco,
            @Schema(description = "Unidades em estoque", example = "10") int estoqueAtual) {}
}
