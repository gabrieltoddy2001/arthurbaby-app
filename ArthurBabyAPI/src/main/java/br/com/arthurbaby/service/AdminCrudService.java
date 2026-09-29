package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.ConfiguracaoLoja;
import br.com.arthurbaby.entity.Cor;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Favorito;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Modelo;
import br.com.arthurbaby.entity.MovimentacaoEstoque;
import br.com.arthurbaby.entity.Pedido;
import br.com.arthurbaby.entity.PedidoItem;
import br.com.arthurbaby.entity.PedidoStatusHistorico;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import br.com.arthurbaby.entity.ProdutoVariacao;
import br.com.arthurbaby.entity.Tamanho;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.CategoriaRepository;
import br.com.arthurbaby.repository.ConfiguracaoLojaRepository;
import br.com.arthurbaby.repository.CorRepository;
import br.com.arthurbaby.repository.EnderecoRepository;
import br.com.arthurbaby.repository.FavoritoRepository;
import br.com.arthurbaby.repository.MarcaRepository;
import br.com.arthurbaby.repository.ModeloRepository;
import br.com.arthurbaby.repository.MovimentacaoEstoqueRepository;
import br.com.arthurbaby.repository.PedidoItemRepository;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.repository.PedidoStatusHistoricoRepository;
import br.com.arthurbaby.repository.ProdutoImagemRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import br.com.arthurbaby.repository.TamanhoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminCrudService {
    private final Map<String, RecursoAdmin> recursos;
    private final ObjectMapper mapper;

    public AdminCrudService(UsuarioRepository usuarios, EnderecoRepository enderecos, CategoriaRepository categorias,
                            MarcaRepository marcas, ProdutoRepository produtos, ProdutoImagemRepository imagens,
                            TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                            ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos,
                            PedidoRepository pedidos, PedidoItemRepository itens,
                            PedidoStatusHistoricoRepository historicos, ConfiguracaoLojaRepository configs,
                            FavoritoRepository favoritos, ObjectMapper mapper) {
        this.mapper = mapper;
        this.recursos = Map.ofEntries(
                recurso("usuarios", usuarios, Usuario.class),
                recurso("enderecos", enderecos, Endereco.class),
                recurso("categorias", categorias, Categoria.class),
                recurso("marcas", marcas, Marca.class),
                recurso("produtos", produtos, Produto.class),
                recurso("produto-imagens", imagens, ProdutoImagem.class),
                recurso("tamanhos", tamanhos, Tamanho.class),
                recurso("cores", cores, Cor.class),
                recurso("modelos", modelos, Modelo.class),
                recurso("produto-variacoes", variacoes, ProdutoVariacao.class),
                recurso("movimentacoes-estoque", movimentos, MovimentacaoEstoque.class),
                recurso("pedidos", pedidos, Pedido.class),
                recurso("pedido-itens", itens, PedidoItem.class),
                recurso("pedido-status-historicos", historicos, PedidoStatusHistorico.class),
                recurso("configuracoes-loja", configs, ConfiguracaoLoja.class),
                recurso("favoritos", favoritos, Favorito.class)
        );
    }

    public List<?> listar(String tipo) {
        return recurso(tipo).repository().findAll();
    }

    public Object buscar(String tipo, Long id) {
        return recurso(tipo).repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));
    }

    public Object criar(String tipo, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        return recurso.repository().save(mapper.convertValue(body, recurso.entityClass()));
    }

    public Object atualizar(String tipo, Long id, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));

        Object entity = mapper.convertValue(body, recurso.entityClass());
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
        return recurso.repository().save(entity);
    }

    public void excluir(String tipo, Long id) {
        RecursoAdmin recurso = recurso(tipo);
        recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));
        recurso.repository().deleteById(id);
    }

    private RecursoAdmin recurso(String tipo) {
        RecursoAdmin recurso = recursos.get(tipo);

        if (recurso == null) {
            throw new IllegalArgumentException("Tipo de recurso invalido: " + tipo);
        }

        return recurso;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map.Entry<String, RecursoAdmin> recurso(String tipo, JpaRepository repository, Class<?> entityClass) {
        return Map.entry(tipo, new RecursoAdmin((JpaRepository<Object, Long>) repository, entityClass));
    }

    private record RecursoAdmin(JpaRepository<Object, Long> repository, Class<?> entityClass) {
    }
}
