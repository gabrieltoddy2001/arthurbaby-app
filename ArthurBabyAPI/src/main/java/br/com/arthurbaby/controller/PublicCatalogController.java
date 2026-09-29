package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.CategoriaResponse;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse.VariacaoResponse;
import br.com.arthurbaby.dto.ProdutoResumoResponse;
import br.com.arthurbaby.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Catálogo", description = "Categorias, produtos e estoque. Endpoints públicos (não exigem token).")
public class PublicCatalogController {
    private final CatalogoService catalogo;
    public PublicCatalogController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }
    @GetMapping("/api/categorias")
    @Operation(summary = "Listar categorias", description = "Retorna a árvore de categorias do catálogo, com as subcategorias aninhadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Árvore de categorias")
    })
    public List<CategoriaResponse> categorias() { return catalogo.arvoreCategorias(); }
    @GetMapping("/api/produtos")
    @Operation(summary = "Listar produtos", description = "Retorna os produtos do catálogo de forma paginada, com filtros opcionais por texto (nome, código, SKU ou descrição), categoria, faixa de preço e promoção.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de produtos que atendem aos filtros"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de filtro ou de paginação com formato inválido")
    })
    public Page<ProdutoResumoResponse> produtos(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) Long categoriaId,
                                                @RequestParam(required = false) BigDecimal precoMin,
                                                @RequestParam(required = false) BigDecimal precoMax,
                                                @RequestParam(required = false) Boolean promocao,
                                                Pageable pageable) {
        return catalogo.listarProdutos(q, categoriaId, precoMin, precoMax, promocao, pageable);
    }
    @GetMapping("/api/produtos/{id}")
    @Operation(summary = "Buscar produto por id", description = "Retorna os detalhes do produto identificado pelo id informado, incluindo imagens e variações.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes do produto"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ProdutoDetalheResponse produto(@PathVariable Long id) {
        return catalogo.detalhe(id);
    }
    @GetMapping("/api/estoque/{produtoId}")
    @Operation(summary = "Consultar estoque do produto", description = "Retorna as variações (tamanho, cor, modelo) do produto informado com as quantidades em estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Variações do produto com estoque (vazia se o produto não existir)")
    })
    public List<VariacaoResponse> estoque(@PathVariable Long produtoId) {
        return catalogo.estoque(produtoId);
    }
    @GetMapping("/api/estoque/{produtoId}/movimentacoes")
    @Operation(summary = "Listar movimentações de estoque", description = "Retorna o histórico de entradas e saídas de estoque do produto informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentações do produto (vazia se não houver)")
    })
    public List<MovimentacaoEstoqueResponse> movimentacoes(@PathVariable Long produtoId) {
        return catalogo.movimentacoes(produtoId);
    }
}
