package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** Cadastro de produto com imagens e variacoes numa unica chamada. */
@Schema(description = "Produto completo a cadastrar (dados, imagens e variações)")
public record ProdutoCompletoRequest(
        @Schema(description = "Id da categoria", example = "1") Long categoriaId,
        @Schema(description = "Id da marca", example = "1") Long marcaId,
        @Schema(description = "Código interno (único)", example = "AB-002") String codigo,
        @Schema(description = "SKU do produto (único)", example = "AB-002") String sku,
        @Schema(description = "Nome", example = "Body Manga Longa Algodão") String nome,
        @Schema(description = "Descrição", example = "Body 100% algodão com abertura na entreperna") String descricao,
        @Schema(description = "Preço de venda", example = "39.90") BigDecimal preco,
        @Schema(description = "Preço promocional (opcional, menor que o preço)", example = "34.90") BigDecimal precoPromocional,
        @Schema(description = "Exibir na vitrine de destaques", example = "false") boolean destaque,
        @Schema(description = "Produto em promoção", example = "true") boolean promocao,
        @Schema(description = "Imagens do produto") List<ImagemRequest> imagens,
        @Schema(description = "Variações (tamanho/cor/modelo) com estoque inicial") List<VariacaoRequest> variacoes) {

    /** Imagem do produto. Se nenhuma for marcada como principal, a primeira assume. */
    @Schema(description = "Imagem do produto")
    public record ImagemRequest(
            @Schema(description = "URL pública da imagem", example = "https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto") String url,
            @Schema(description = "Texto alternativo", example = "Body branco - frente") String descricao,
            @Schema(description = "Imagem principal da vitrine", example = "true") boolean principal) {}

    /** Variação vendável; o estoque inicial vira uma movimentação de ENTRADA. */
    @Schema(description = "Variação do produto")
    public record VariacaoRequest(
            @Schema(description = "SKU da variação (único)", example = "AB-002-P-BR") String sku,
            @Schema(description = "Id do tamanho", example = "2") Long tamanhoId,
            @Schema(description = "Id da cor", example = "1") Long corId,
            @Schema(description = "Id do modelo", example = "1") Long modeloId,
            @Schema(description = "Estoque inicial", example = "15") int estoqueAtual,
            @Schema(description = "Preço específico da variação (opcional; usa o do produto se vazio)", example = "39.90") BigDecimal preco) {}
}
