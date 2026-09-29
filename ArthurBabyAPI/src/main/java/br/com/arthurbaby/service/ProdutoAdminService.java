package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.ProdutoCompletoRequest;
import br.com.arthurbaby.dto.ProdutoCompletoRequest.ImagemRequest;
import br.com.arthurbaby.dto.ProdutoCompletoRequest.VariacaoRequest;
import br.com.arthurbaby.dto.ProdutoDetalheResponse;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import br.com.arthurbaby.entity.ProdutoVariacao;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Cadastro completo de produto (dados + imagens + variacoes) em uma transacao: ou grava tudo ou nada. */
@Service
public class ProdutoAdminService {
    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final MarcaRepository marcas;
    private final TamanhoRepository tamanhos;
    private final CorRepository cores;
    private final ModeloRepository modelos;
    private final ProdutoVariacaoRepository variacoesRepo;
    private final EstoqueService estoque;
    private final CatalogoService catalogo;

    public ProdutoAdminService(ProdutoRepository produtos, CategoriaRepository categorias, MarcaRepository marcas,
                               TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                               ProdutoVariacaoRepository variacoesRepo, EstoqueService estoque, CatalogoService catalogo) {
        this.produtos = produtos;
        this.variacoesRepo = variacoesRepo;
        this.categorias = categorias;
        this.marcas = marcas;
        this.tamanhos = tamanhos;
        this.cores = cores;
        this.modelos = modelos;
        this.estoque = estoque;
        this.catalogo = catalogo;
    }

    @Transactional
    public ProdutoDetalheResponse cadastrarCompleto(ProdutoCompletoRequest request, Usuario usuario) {
        obrigatorio(request.nome(), "Nome e obrigatorio");
        obrigatorio(request.codigo(), "Codigo e obrigatorio");
        obrigatorio(request.sku(), "SKU e obrigatorio");
        if (request.preco() == null || request.preco().signum() <= 0) throw new IllegalArgumentException("Preco deve ser maior que zero");
        if (request.precoPromocional() != null && request.precoPromocional().compareTo(request.preco()) >= 0) {
            throw new IllegalArgumentException("Preco promocional deve ser menor que o preco");
        }
        if (request.categoriaId() == null) throw new IllegalArgumentException("Categoria e obrigatoria");
        if (produtos.existsByCodigo(request.codigo().trim())) throw new IllegalArgumentException("Codigo de produto ja cadastrado");
        if (produtos.existsBySku(request.sku().trim())) throw new IllegalArgumentException("SKU de produto ja cadastrado");

        Produto produto = new Produto();
        produto.setCategoria(buscar(categorias, request.categoriaId(), "Categoria"));
        if (request.marcaId() != null) produto.setMarca(buscar(marcas, request.marcaId(), "Marca"));
        produto.setCodigo(request.codigo().trim());
        produto.setSku(request.sku().trim());
        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setPrecoPromocional(request.precoPromocional());
        produto.setDestaque(request.destaque());
        produto.setPromocao(request.promocao());

        adicionarImagens(produto, request.imagens());
        adicionarVariacoes(produto, request.variacoes());
        Produto salvo = produtos.saveAndFlush(produto);

        // Estoque inicial fica registrado como ENTRADA para manter o historico de movimentacoes consistente.
        for (ProdutoVariacao variacao : salvo.getVariacoes()) {
            int inicial = variacao.getEstoqueAtual();
            if (inicial > 0) {
                estoque.registrar(salvo, variacao, usuario, MovimentoEstoqueTipo.ENTRADA, inicial, 0, inicial,
                        "Estoque inicial do cadastro");
            }
        }
        return catalogo.detalhe(salvo.getId());
    }

    private void adicionarImagens(Produto produto, List<ImagemRequest> imagens) {
        if (imagens == null || imagens.isEmpty()) return;
        // Exatamente uma imagem principal: a primeira marcada ou, se nenhuma for, a primeira da lista.
        int indicePrincipal = 0;
        for (int i = 0; i < imagens.size(); i++) {
            if (imagens.get(i).principal()) { indicePrincipal = i; break; }
        }
        for (int i = 0; i < imagens.size(); i++) {
            ImagemRequest req = imagens.get(i);
            obrigatorio(req.url(), "URL da imagem e obrigatoria");
            ProdutoImagem img = new ProdutoImagem();
            img.setProduto(produto);
            img.setUrl(req.url().trim());
            img.setDescricao(req.descricao());
            img.setPrincipal(i == indicePrincipal);
            img.setOrdemExibicao(i + 1);
            produto.getImagens().add(img);
        }
    }

    private void adicionarVariacoes(Produto produto, List<VariacaoRequest> variacoes) {
        if (variacoes == null || variacoes.isEmpty()) return;
        Set<String> skus = new HashSet<>();
        for (VariacaoRequest req : variacoes) {
            obrigatorio(req.sku(), "SKU da variacao e obrigatorio");
            String sku = req.sku().trim();
            if (!skus.add(sku)) throw new IllegalArgumentException("SKU de variacao repetido: " + sku);
            if (variacoesRepo.existsBySku(sku)) throw new IllegalArgumentException("SKU de variacao ja cadastrado: " + sku);
            if (req.estoqueAtual() < 0) throw new IllegalArgumentException("Estoque inicial nao pode ser negativo");
            if (req.preco() != null && req.preco().signum() <= 0) throw new IllegalArgumentException("Preco da variacao deve ser maior que zero");

            ProdutoVariacao v = new ProdutoVariacao();
            v.setProduto(produto);
            v.setSku(sku);
            if (req.tamanhoId() != null) v.setTamanho(buscar(tamanhos, req.tamanhoId(), "Tamanho"));
            if (req.corId() != null) v.setCor(buscar(cores, req.corId(), "Cor"));
            if (req.modeloId() != null) v.setModelo(buscar(modelos, req.modeloId(), "Modelo"));
            v.setEstoqueAtual(req.estoqueAtual());
            v.setPreco(req.preco());
            produto.getVariacoes().add(v);
        }
    }

    private static <T> T buscar(JpaRepository<T, Long> repo, Long id, String nome) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException(nome + " nao encontrado(a): id " + id));
    }

    private static void obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
    }
}
