package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.CategoriaResponse;
import br.com.arthurbaby.dto.CategoriaResponse.SubcategoriaResponse;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse.ImagemResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse.VariacaoResponse;
import br.com.arthurbaby.dto.ProdutoResumoResponse;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import br.com.arthurbaby.entity.Enums.VariacaoStatus;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoVariacao;
import br.com.arthurbaby.repository.CategoriaRepository;
import br.com.arthurbaby.repository.MovimentacaoEstoqueRepository;
import br.com.arthurbaby.repository.ProdutoImagemRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CatalogoService {
    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;
    private final ProdutoImagemRepository imagens;
    private final ProdutoVariacaoRepository variacoes;
    private final MovimentacaoEstoqueRepository movimentos;
    public CatalogoService(CategoriaRepository categorias, ProdutoRepository produtos, ProdutoImagemRepository imagens,
                           ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos) {
        this.categorias = categorias;
        this.produtos = produtos;
        this.imagens = imagens;
        this.variacoes = variacoes;
        this.movimentos = movimentos;
    }

    /** Arvore de categorias ativas: raizes com suas subcategorias ativas, na ordem de exibicao. */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> arvoreCategorias() {
        return categorias.findByCategoriaPaiIsNullAndStatusOrderByOrdemExibicaoAsc(CategoriaStatus.ATIVA).stream()
                .map(raiz -> new CategoriaResponse(raiz.getId(), raiz.getNome(), raiz.getIcone(),
                        categorias.findByCategoriaPaiId(raiz.getId()).stream()
                                .filter(sub -> sub.getStatus() == CategoriaStatus.ATIVA)
                                .sorted(Comparator.comparingInt(Categoria::getOrdemExibicao))
                                .map(sub -> new SubcategoriaResponse(sub.getId(), sub.getNome()))
                                .toList()))
                .toList();
    }

    /** Listagem paginada com filtros opcionais por texto (nome, codigo, SKU ou descricao), categoria, preco e promocao. */
    @Transactional(readOnly = true)
    public Page<ProdutoResumoResponse> listarProdutos(String q, Long categoriaId, BigDecimal precoMin, BigDecimal precoMax,
                                                      Boolean promocao, Pageable pageable) {
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
        return produtos.findAll(spec, pageable).map(ProdutoResumoResponse::de);
    }

    @Transactional(readOnly = true)
    public ProdutoDetalheResponse detalhe(Long id) {
        Produto produto = produtos.findById(id).orElseThrow(() -> new NoSuchElementException("Produto nao encontrado"));
        Categoria categoria = produto.getCategoria();
        Marca marca = produto.getMarca();
        return new ProdutoDetalheResponse(
                produto.getId(), produto.getCodigo(), produto.getSku(), produto.getNome(), produto.getDescricao(),
                produto.getPreco(), produto.getPrecoPromocional(), produto.isPromocao(), produto.isDestaque(),
                produto.getStatus().name(),
                categoria != null ? categoria.getId() : null, categoria != null ? categoria.getNome() : null,
                marca != null ? marca.getId() : null, marca != null ? marca.getNome() : null,
                produto.getAvaliacao(),
                imagens.findByProdutoIdOrderByOrdemExibicaoAsc(id).stream().map(ImagemResponse::de).toList(),
                variacoes.findByProdutoIdAndStatus(id, VariacaoStatus.ATIVA).stream().map(CatalogoService::toVariacao).toList());
    }

    /** Todas as variacoes do produto (ativas ou nao) com o estoque atual. */
    @Transactional(readOnly = true)
    public List<VariacaoResponse> estoque(Long produtoId) {
        return variacoes.findByProdutoId(produtoId).stream().map(CatalogoService::toVariacao).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse> movimentacoes(Long produtoId) {
        return movimentos.findByProdutoId(produtoId).stream().map(MovimentacaoEstoqueResponse::de).toList();
    }

    public static VariacaoResponse toVariacao(ProdutoVariacao v) {
        return new VariacaoResponse(v.getId(), v.getSku(),
                v.getTamanho() != null ? v.getTamanho().getNome() : null,
                v.getCor() != null ? v.getCor().getNome() : null,
                v.getModelo() != null ? v.getModelo().getNome() : null,
                v.getPreco(), v.getEstoqueAtual());
    }
}
