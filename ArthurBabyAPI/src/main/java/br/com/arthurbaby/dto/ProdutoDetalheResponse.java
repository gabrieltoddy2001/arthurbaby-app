package br.com.arthurbaby.dto;
import br.com.arthurbaby.entity.ProdutoImagem;
import java.math.BigDecimal;
import java.util.List;
public record ProdutoDetalheResponse(
        Long id, String codigo, String sku, String nome, String descricao,
        BigDecimal preco, BigDecimal precoPromocional, boolean promocao, boolean destaque, String status,
        Long categoriaId, String categoriaNome, Long marcaId, String marca,
        BigDecimal avaliacao, List<ImagemResponse> imagens, List<VariacaoResponse> variacoes) {
    public record ImagemResponse(Long id, String url, String descricao, boolean principal) {
        public static ImagemResponse de(ProdutoImagem i) {
            return new ImagemResponse(i.getId(), i.getUrl(), i.getDescricao(), i.isPrincipal());
        }
    }
    public record VariacaoResponse(Long id, String sku, String tamanho, String cor, String modelo,
                                   BigDecimal preco, int estoqueAtual) {}
}
