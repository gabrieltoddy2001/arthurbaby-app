package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.CategoriaRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminCrudServiceTest {
    private CategoriaRepository categorias;
    private UsuarioRepository usuarios;
    private PasswordEncoder encoder;
    private AdminCrudService service;

    @BeforeEach
    void setUp() {
        categorias = mock(CategoriaRepository.class);
        usuarios = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        service = new AdminCrudService(
                usuarios,
                mock(br.com.arthurbaby.repository.EnderecoRepository.class),
                categorias,
                mock(br.com.arthurbaby.repository.MarcaRepository.class),
                mock(br.com.arthurbaby.repository.ProdutoRepository.class),
                mock(br.com.arthurbaby.repository.ProdutoImagemRepository.class),
                mock(br.com.arthurbaby.repository.TamanhoRepository.class),
                mock(br.com.arthurbaby.repository.CorRepository.class),
                mock(br.com.arthurbaby.repository.ModeloRepository.class),
                mock(br.com.arthurbaby.repository.ProdutoVariacaoRepository.class),
                mock(br.com.arthurbaby.repository.MovimentacaoEstoqueRepository.class),
                mock(br.com.arthurbaby.repository.PedidoRepository.class),
                mock(br.com.arthurbaby.repository.PedidoItemRepository.class),
                mock(br.com.arthurbaby.repository.PedidoStatusHistoricoRepository.class),
                mock(br.com.arthurbaby.repository.ConfiguracaoLojaRepository.class),
                mock(br.com.arthurbaby.repository.FavoritoRepository.class),
                mock(br.com.arthurbaby.repository.AuditoriaRepository.class),
                mapper, encoder);
    }

    @Test
    void listarEncaminhaParaRepositorioEDevolveJson() {
        Categoria categoria = new Categoria();
        categoria.setNome("Roupas");
        when(categorias.findAll()).thenReturn(List.of(categoria));

        List<Map<String, Object>> resultado = service.listar("categorias");

        assertEquals(1, resultado.size());
        assertEquals("Roupas", resultado.getFirst().get("nome"));
        verify(categorias).findAll();
    }

    @Test
    void criarConverteBodyParaEntidadeDoRecurso() {
        when(categorias.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> resultado = service.criar("categorias", Map.of("nome", "Roupas", "ordemExibicao", 3));

        ArgumentCaptor<Categoria> salvo = ArgumentCaptor.forClass(Categoria.class);
        verify(categorias).save(salvo.capture());
        assertEquals("Roupas", salvo.getValue().getNome());
        assertEquals(3, salvo.getValue().getOrdemExibicao());
        assertEquals("Roupas", resultado.get("nome"));
    }

    @Test
    void atualizarForcaIdDaRotaSobreIdEnviadoNoBody() {
        Categoria existente = new Categoria();
        existente.setId(8L);
        when(categorias.findById(8L)).thenReturn(Optional.of(existente));
        when(categorias.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> resultado = service.atualizar("categorias", 8L, Map.of("id", 99L, "nome", "Atualizada"));

        assertEquals(8, ((Number) resultado.get("id")).intValue());
        assertEquals("Atualizada", resultado.get("nome"));
    }

    @Test
    void usuarioNuncaExpoeSenhaNemGettersDoSpringSecurity() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNomeCompleto("Ana");
        usuario.setEmail("ana@example.com");
        usuario.setSenha("$2a$10$VSMLGFthR87nW3Stq7rbZOsXbN.7wd4wdUz47719pA3zE0OWsms4u");
        Endereco endereco = new Endereco();
        endereco.setCidade("Salvador");
        usuario.getEnderecos().add(endereco);
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario));

        Map<String, Object> json = service.buscar("usuarios", 3L);

        assertFalse(json.containsKey("senha"));
        assertFalse(json.containsKey("password"));
        assertFalse(json.containsKey("authorities"));
        assertEquals("ana@example.com", json.get("email"));
        assertTrue(json.get("enderecos") instanceof List<?> lista && lista.size() == 1);
    }

    @Test
    void criarUsuarioCodificaSenhaEmTextoENormalizaEmail() {
        when(encoder.encode("123456")).thenReturn("$2a$10$abcdefghijklmnopqrstuvabcdefghijklmnopqrstuvwxyz01234");
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.criar("usuarios", Map.of("nomeCompleto", "Ana", "email", " ANA@Example.com ", "senha", "123456"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(salvo.capture());
        assertEquals("$2a$10$abcdefghijklmnopqrstuvabcdefghijklmnopqrstuvwxyz01234", salvo.getValue().getSenha());
        assertEquals("ana@example.com", salvo.getValue().getEmail());
    }

    @Test
    void atualizarUsuarioSemSenhaMantemHashAtual() {
        Usuario atual = new Usuario();
        atual.setId(3L);
        atual.setSenha("hash-atual");
        when(usuarios.findById(3L)).thenReturn(Optional.of(atual));
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizar("usuarios", 3L, Map.of("nomeCompleto", "Ana", "email", "ana@example.com"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(salvo.capture());
        assertEquals("hash-atual", salvo.getValue().getSenha());
    }

    @Test
    void excluirSoRemoveRegistroExistente() {
        when(categorias.findById(8L)).thenReturn(Optional.of(new Categoria()));

        service.excluir("categorias", 8L);

        verify(categorias).deleteById(8L);
    }

    @Test
    void recursoInvalidoEhRejeitadoAntesDeConsultarRepositorio() {
        assertThrows(IllegalArgumentException.class, () -> service.buscar("desconhecido", 1L));

        verifyNoInteractions(categorias);
    }
}
