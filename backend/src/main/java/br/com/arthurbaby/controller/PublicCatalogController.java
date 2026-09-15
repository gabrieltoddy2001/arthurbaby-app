package br.com.arthurbaby.controller;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
public class PublicCatalogController {
    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;
    private final ProdutoVariacaoRepository variacoes;
    private final MovimentacaoEstoqueRepository movimentos;
    public PublicCatalogController(CategoriaRepository categorias, ProdutoRepository produtos,
                                   ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos) {
        this.categorias = categorias;
        this.produtos = produtos;
        this.variacoes = variacoes;
        this.movimentos = movimentos;
    }
    @GetMapping("/api/categorias") public List<Categoria> categorias() { return categorias.findAll(); }
    @GetMapping("/api/produtos")
    public Page<ProdutoResponse> produtos(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) Long categoriaId,
                                          @RequestParam(required = false) BigDecimal precoMin,
                                          @RequestParam(required = false) BigDecimal precoMax,
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
        return produtos.findAll(spec, pageable).map(this::toResponse);
    }
    @GetMapping("/api/produtos/{id}") public ProdutoResponse produto(@PathVariable Long id) {
        return produtos.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
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
