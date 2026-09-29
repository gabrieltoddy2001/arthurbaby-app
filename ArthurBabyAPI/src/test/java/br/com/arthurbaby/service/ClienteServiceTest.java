package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.AtualizarClienteRequest;
import br.com.arthurbaby.dto.EnderecoRequest;
import br.com.arthurbaby.dto.EnderecoResponse;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.EnderecoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServiceTest {
    private UsuarioRepository usuarios;
    private EnderecoRepository enderecos;
    private ClienteService service;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        enderecos = mock(EnderecoRepository.class);
        service = new ClienteService(usuarios, enderecos);
        cliente = new Usuario();
        cliente.setId(4L);
        cliente.setNomeCompleto("Ana Lima");
        cliente.setEmail("ana@example.com");
        cliente.setCpf("52998224725");
        when(usuarios.findById(4L)).thenReturn(Optional.of(cliente));
    }

    @Test
    void atualizarPerfilNormalizaEmailEFormataCpf() {
        when(usuarios.existsByEmail("novo@example.com")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.atualizarPerfil(4L, new AtualizarClienteRequest(
                "Ana Maria", " NOVO@EXAMPLE.COM ", "529.982.247-25", "11999990000"));

        assertEquals("novo@example.com", response.email());
        assertEquals("52998224725", response.cpf());
        assertEquals("Ana Maria", response.nomeCompleto());
        assertEquals("11999990000", response.telefone());
    }

    @Test
    void atualizarPerfilRejeitaCpfInvalidoEEmailDuplicado() {
        assertThrows(IllegalArgumentException.class, () -> service.atualizarPerfil(4L,
                new AtualizarClienteRequest(null, null, "111.111.111-11", null)));

        when(usuarios.existsByEmail("ocupado@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.atualizarPerfil(4L,
                new AtualizarClienteRequest(null, "ocupado@example.com", null, null)));
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void inativarAtualizaStatusDoCliente() {
        service.inativar(4L);

        assertEquals(UsuarioStatus.INATIVO, cliente.getStatus());
        verify(usuarios).save(cliente);
    }

    @Test
    void criarEnderecoMarcaComoPrincipalQuandoPrimeiroEndereco() {
        when(enderecos.findByUsuarioId(4L)).thenReturn(List.of());
        when(enderecos.save(any(Endereco.class))).thenAnswer(invocation -> {
            Endereco salvo = invocation.getArgument(0);
            salvo.setId(12L);
            return salvo;
        });

        EnderecoResponse response = service.criarEndereco(4L, enderecoRequest(null));

        assertEquals(12L, response.id());
        assertTrue(response.principal());
        assertEquals("Campinas", response.cidade());
    }

    @Test
    void criarEnderecoPrincipalDesmarcaEnderecoPrincipalAnterior() {
        Endereco anterior = new Endereco();
        anterior.setPrincipal(true);
        when(enderecos.findByUsuarioId(4L)).thenReturn(List.of(anterior));
        when(enderecos.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnderecoResponse response = service.criarEndereco(4L, enderecoRequest(true));

        assertFalse(anterior.isPrincipal());
        assertTrue(response.principal());
        verify(enderecos).saveAll(List.of(anterior));
    }

    @Test
    void buscarEnderecoRejeitaEnderecoDeOutroClienteOuInexistente() {
        when(enderecos.findByIdAndUsuarioId(99L, 4L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.buscarEndereco(4L, 99L));
    }

    private static EnderecoRequest enderecoRequest(Boolean principal) {
        return new EnderecoRequest("13000-000", "Rua das Flores", "10", null,
                "Centro", "Campinas", "SP", null, principal);
    }
}
