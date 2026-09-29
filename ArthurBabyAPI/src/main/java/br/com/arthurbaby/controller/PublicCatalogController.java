package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.CategoriaResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.repository.*;
import br.com.arthurbaby.service.CatalogoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class PublicCatalogController {
    private final ProdutoRepository produtos;
    private final ProdutoVariacaoRepository variacoes;
    private final MovimentacaoEstoqueRepository movimentos;
    private final CatalogoService catalogo;
    public PublicCatalogController(ProdutoRepository produtos, ProdutoVariacaoRepository variacoes,
                                   MovimentacaoEstoqueRepository movimentos, CatalogoService catalogo) {
        this.produtos = produtos;
        this.variacoes = variacoes;
        this.movimentos = movimentos;
        this.catalogo = catalogo;
    }
    @GetMapping("/api/categorias") public List<CategoriaResponse> categorias() { return catalogo.arvoreCategorias(); }
    @GetMapping("/api/produtos")
    public Page<ProdutoResponse> produtos(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) Long categoriaId,
                                          @RequestParam(required = false) BigDecimal precoMin,
                                          @RequestParam(required = false) BigDecimal precoMax,
                                          @RequestParam(required = false) Boolean promocao,
                                          Pageable pageable) {
        Specification<Produto> spec = Specification.where(null);
        if (q != null && !q.isBlank()) {
            String term = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nome")), term),
                    cb.like(cb.lower(root.get("codigo")), term),
                    cb.like(cb.lower(root.get("sku")), term),
                    cb.like(cb.lower(root.get("descricao")), term)));
        }
        if (categoriaId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        if (precoMin != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("preco"), precoMin));
        if (precoMax != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("preco"), precoMax));
        if (Boolean.TRUE.equals(promocao)) spec = spec.and((root, query, cb) -> cb.isTrue(root.get("promocao")));
        return produtos.findAll(spec, pageable).map(this::toResponse);
    }
    @GetMapping("/api/produtos/{id}") public ProdutoDetalheResponse produto(@PathVariable Long id) {
        return catalogo.detalhe(id);
    }
    @GetMapping("/api/estoque/{produtoId}") public List<ProdutoVariacao> estoque(@PathVariable Long produtoId) {
        return variacoes.findByProdutoId(produtoId);
    }
    @GetMapping("/api/estoque/{produtoId}/movimentacoes") public List<MovimentacaoEstoque> movimentacoes(@PathVariable Long produtoId) {
        return movimentos.findByProdutoId(produtoId);
    }

    private ProdutoResponse toResponse(Produto produto) {
        Categoria categoria = produto.getCategoria();
        Marca marca = produto.getMarca();
        return new ProdutoResponse(
                produto.getId(),
                produto.getCodigo(),
                produto.getSku(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getPrecoPromocional(),
                produto.isPromocao(),
                produto.isDestaque(),
                produto.getStatus().name(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                marca != null ? marca.getId() : null,
                marca != null ? marca.getNome() : null);
    }

    public record ProdutoResponse(
            Long id,
            String codigo,
            String sku,
            String nome,
            String descricao,
            BigDecimal preco,
            BigDecimal precoPromocional,
            boolean promocao,
            boolean destaque,
            String status,
            Long categoriaId,
            String categoriaNome,
            Long marcaId,
            String marcaNome) {
    }
}
