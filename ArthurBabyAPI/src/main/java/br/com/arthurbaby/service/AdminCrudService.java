package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Auditoria;
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
import br.com.arthurbaby.repository.AuditoriaRepository;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CRUD generico do painel admin. As entidades nunca saem do servico: sao convertidas em mapas JSON
 * dentro da transacao, entao colecoes lazy sao lidas enquanto a sessao do JPA esta aberta.
 */
@Service
public class AdminCrudService {
    private static final TypeReference<Map<String, Object>> JSON = new TypeReference<>() {};
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.{53}$");

    private final Map<String, RecursoAdmin> recursos;
    private final ObjectMapper mapper;
    private final PasswordEncoder encoder;

    public AdminCrudService(UsuarioRepository usuarios, EnderecoRepository enderecos, CategoriaRepository categorias,
                            MarcaRepository marcas, ProdutoRepository produtos, ProdutoImagemRepository imagens,
                            TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                            ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos,
                            PedidoRepository pedidos, PedidoItemRepository itens,
                            PedidoStatusHistoricoRepository historicos, ConfiguracaoLojaRepository configs,
                            FavoritoRepository favoritos, AuditoriaRepository auditoria, ObjectMapper mapper, PasswordEncoder encoder) {
        this.mapper = mapper;
        this.encoder = encoder;
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
                recurso("favoritos", favoritos, Favorito.class),
                recurso("auditoria", auditoria, Auditoria.class)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(String tipo) {
        return recurso(tipo).repository().findAll().stream().map(this::json).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buscar(String tipo, Long id) {
        return json(recurso(tipo).repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado")));
    }

    @Transactional
    public Map<String, Object> criar(String tipo, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        Object entity = mapper.convertValue(body, recurso.entityClass());
        prepararUsuario(entity, null);
        return json(recurso.repository().save(entity));
    }

    @Transactional
    public Map<String, Object> atualizar(String tipo, Long id, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        Object existente = recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));

        Object entity = mapper.convertValue(body, recurso.entityClass());
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
        prepararUsuario(entity, existente);
        return json(recurso.repository().save(entity));
    }

    @Transactional
    public void excluir(String tipo, Long id) {
        RecursoAdmin recurso = recurso(tipo);
        recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));
        recurso.repository().deleteById(id);
    }

    private Map<String, Object> json(Object entity) {
        return mapper.convertValue(entity, JSON);
    }

    /** Usuario criado/alterado pelo admin: senha em texto vira hash BCrypt; sem senha no corpo, mantem a atual. */
    private void prepararUsuario(Object entity, Object existente) {
        if (!(entity instanceof Usuario usuario)) return;
        usuario.setEmail(AuthService.normalizarEmail(usuario.getEmail()));
        String senha = usuario.getSenha();
        if (senha == null || senha.isBlank()) {
            if (existente instanceof Usuario atual) usuario.setSenha(atual.getSenha());
        } else if (!BCRYPT.matcher(senha).matches()) {
            usuario.setSenha(encoder.encode(senha));
        }
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
