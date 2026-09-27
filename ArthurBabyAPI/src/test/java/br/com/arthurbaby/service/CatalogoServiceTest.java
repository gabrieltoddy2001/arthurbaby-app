package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.CategoriaResponse;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Cor;
import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Modelo;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import br.com.arthurbaby.entity.ProdutoVariacao;
import br.com.arthurbaby.entity.Tamanho;
import br.com.arthurbaby.repository.CategoriaRepository;
import br.com.arthurbaby.repository.ProdutoImagemRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CatalogoServiceTest {
    private CategoriaRepository categorias;
    private ProdutoRepository produtos;
    private ProdutoImagemRepository imagens;
    private ProdutoVariacaoRepository variacoes;
    private CatalogoService service;

    @BeforeEach
    void setUp() {
        categorias = mock(CategoriaRepository.class);
        produtos = mock(ProdutoRepository.class);
        imagens = mock(ProdutoImagemRepository.class);
        variacoes = mock(ProdutoVariacaoRepository.class);
        service = new CatalogoService(categorias, produtos, imagens, variacoes);
    }

    @Test
    void arvoreCategoriasFiltraInativasEOrdenaSubcategorias() {
        Categoria raiz = categoria(1L, "Roupas", CategoriaStatus.ATIVA, 0);
        Categoria segunda = categoria(3L, "Blusas", CategoriaStatus.ATIVA, 2);
        Categoria primeira = categoria(2L, "Bodies", CategoriaStatus.ATIVA, 1);
        Categoria inativa = categoria(4L, "Oculta", CategoriaStatus.INATIVA, 0);
        when(categorias.findByCategoriaPaiIsNullAndStatusOrderByOrdemExibicaoAsc(CategoriaStatus.ATIVA))
                .thenReturn(List.of(raiz));
        when(categorias.findByCategoriaPaiId(1L)).thenReturn(List.of(segunda, inativa, primeira));

        List<CategoriaResponse> response = service.arvoreCategorias();

        assertEquals(1, response.size());
        assertEquals("Roupas", response.getFirst().nome());
        assertEquals(List.of("Bodies", "Blusas"), response.getFirst().subcategorias().stream()
                .map(CategoriaResponse.SubcategoriaResponse::nome).toList());
    }

    @Test
    void detalheMapeiaProdutoImagensEModificadoresDeVariacao() {
        Produto produto = new Produto();
        produto.setId(10L);
        produto.setCodigo("P-10");
        produto.setSku("SKU-10");
        produto.setNome("Body");
        produto.setDescricao("Algodao");
        produto.setPreco(new BigDecimal("30.00"));
        produto.setPrecoPromocional(new BigDecimal("25.00"));
        produto.setPromocao(true);
        produto.setDestaque(true);
        produto.setCategoria(categoria(1L, "Roupas", CategoriaStatus.ATIVA, 0));
        Marca marca = new Marca();
        marca.setId(5L);
        marca.setNome("Marca");
        produto.setMarca(marca);

        ProdutoImagem imagem = new ProdutoImagem();
        imagem.setUrl("https://example.com/body.jpg");
        imagem.setPrincipal(true);

        Tamanho tamanho = new Tamanho();
        tamanho.setNome("M");
        Cor cor = new Cor();
        cor.setNome("Azul");
        Modelo modelo = new Modelo();
        modelo.setNome("Classico");
        ProdutoVariacao variacao = new ProdutoVariacao();
        variacao.setId(20L);
        variacao.setSku("SKU-20");
        variacao.setTamanho(tamanho);
        variacao.setCor(cor);
        variacao.setModelo(modelo);
        variacao.setPreco(new BigDecimal("27.00"));
        variacao.setEstoqueAtual(4);

        when(produtos.findById(10L)).thenReturn(Optional.of(produto));
        when(imagens.findByProdutoIdOrderByOrdemExibicaoAsc(10L)).thenReturn(List.of(imagem));
        when(variacoes.findByProdutoIdAndStatus(10L, br.com.arthurbaby.entity.Enums.VariacaoStatus.ATIVA))
                .thenReturn(List.of(variacao));

        ProdutoDetalheResponse response = service.detalhe(10L);

        assertEquals("Body", response.nome());
        assertEquals("Roupas", response.categoriaNome());
        assertEquals("Marca", response.marca());
        assertEquals("https://example.com/body.jpg", response.imagens().getFirst().url());
        assertEquals("M", response.variacoes().getFirst().tamanho());
        assertEquals("Azul", response.variacoes().getFirst().cor());
        assertEquals("Classico", response.variacoes().getFirst().modelo());
        assertEquals(4, response.variacoes().getFirst().estoqueAtual());
    }

    @Test
    void detalheRejeitaProdutoInexistenteSemConsultarImagensOuVariacoes() {
        when(produtos.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.detalhe(99L));

        verifyNoInteractions(imagens, variacoes);
    }

    private static Categoria categoria(Long id, String nome, CategoriaStatus status, int ordem) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        categoria.setStatus(status);
        categoria.setOrdemExibicao(ordem);
        return categoria;
    }
}
