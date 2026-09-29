package br.com.arthurbaby.dto;
import java.math.BigDecimal;
import java.util.List;
/** Cadastro de produto com imagens e variacoes numa unica chamada. */
public record ProdutoCompletoRequest(
        Long categoriaId, Long marcaId,
        String codigo, String sku, String nome, String descricao,
        BigDecimal preco, BigDecimal precoPromocional,
        boolean destaque, boolean promocao,
        List<ImagemRequest> imagens,
        List<VariacaoRequest> variacoes) {
    public record ImagemRequest(String url, String descricao, boolean principal) {}
    public record VariacaoRequest(String sku, Long tamanhoId, Long corId, Long modeloId, int estoqueAtual, BigDecimal preco) {}
}
