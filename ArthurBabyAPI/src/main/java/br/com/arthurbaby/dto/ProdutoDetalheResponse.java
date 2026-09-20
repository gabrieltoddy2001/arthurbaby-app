package br.com.arthurbaby.dto;
import java.math.BigDecimal;
import java.util.List;
public record ProdutoDetalheResponse(
        Long id, String codigo, String sku, String nome, String descricao,
        BigDecimal preco, BigDecimal precoPromocional, boolean promocao, boolean destaque, String status,
        Long categoriaId, String categoriaNome, Long marcaId, String marca,
        BigDecimal avaliacao, List<ImagemResponse> imagens, List<VariacaoResponse> variacoes) {
    public record ImagemResponse(String url, boolean principal) {}
    public record VariacaoResponse(Long id, String sku, String tamanho, String cor, String modelo,
                                   BigDecimal preco, int estoqueAtual) {}
}
