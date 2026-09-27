package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.repository.CategoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminCrudServiceTest {
    private CategoriaRepository categorias;
    private AdminCrudService service;

    @BeforeEach
    void setUp() {
        categorias = mock(CategoriaRepository.class);
        service = new AdminCrudService(
                mock(br.com.arthurbaby.repository.UsuarioRepository.class),
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
                new ObjectMapper());
    }

    @Test
    void listarEncaminhaParaRepositorioDoRecurso() {
        Categoria categoria = new Categoria();
        categoria.setNome("Roupas");
        when(categorias.findAll()).thenReturn(List.of(categoria));

        List<?> resultado = service.listar("categorias");

        assertEquals(List.of(categoria), resultado);
        verify(categorias).findAll();
    }

    @Test
    void criarConverteBodyParaEntidadeDoRecurso() {
        when(categorias.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Object resultado = service.criar("categorias", Map.of("nome", "Roupas", "ordemExibicao", 3));

        Categoria categoria = assertInstanceOf(Categoria.class, resultado);
        assertEquals("Roupas", categoria.getNome());
        assertEquals(3, categoria.getOrdemExibicao());
        verify(categorias).save(any(Categoria.class));
    }

    @Test
    void atualizarForcaIdDaRotaSobreIdEnviadoNoBody() {
        Categoria existente = new Categoria();
        existente.setId(8L);
        when(categorias.findById(8L)).thenReturn(Optional.of(existente));
        when(categorias.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resultado = (Categoria) service.atualizar("categorias", 8L,
                Map.of("id", 99L, "nome", "Atualizada"));

        assertEquals(8L, resultado.getId());
        assertEquals("Atualizada", resultado.getNome());
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
