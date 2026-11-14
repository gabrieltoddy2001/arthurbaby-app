package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.ClienteAdminResponse;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteAdminServiceTest {
    private UsuarioRepository usuarios;
    private PasswordEncoder encoder;
    private ClienteAdminService service;

    @BeforeEach
    void iniciar() {
        usuarios = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new ClienteAdminService(usuarios, encoder);
    }

    private Usuario usuario(Long id, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNomeCompleto("Maria Souza");
        u.setEmail("maria@example.com");
        u.setPerfil(perfil);
        u.setStatus(UsuarioStatus.ATIVO);
        return u;
    }

    @Test
    void listaApenasClientesMesmoSeRepositorioRetornarUsuariosInternos() {
        when(usuarios.findDistinctByPerfil(Perfil.CLIENTE)).thenReturn(List.of(
                usuario(1L, Perfil.CLIENTE), usuario(2L, Perfil.ADMINISTRADOR), usuario(3L, Perfil.VENDEDOR)));

        List<ClienteAdminResponse> resposta = service.listar();

        assertEquals(1, resposta.size());
        assertEquals(1L, resposta.get(0).id());
        assertEquals(Perfil.CLIENTE, resposta.get(0).perfil());
        verify(usuarios).findDistinctByPerfil(Perfil.CLIENTE);
    }

    @Test
    void alteraSomenteStatusDaContaCliente() {
        Usuario cliente = usuario(7L, Perfil.CLIENTE);
        when(usuarios.findByIdAndPerfil(7L, Perfil.CLIENTE)).thenReturn(Optional.of(cliente));
        when(usuarios.save(cliente)).thenReturn(cliente);

        ClienteAdminResponse resposta = service.alterarStatus(7L, UsuarioStatus.BLOQUEADO);

        assertEquals(UsuarioStatus.BLOQUEADO, resposta.status());
        assertEquals(Perfil.CLIENTE, cliente.getPerfil());
        verify(usuarios).save(cliente);
    }

    @Test
    void naoPermiteInativarUsandoAcaoDeBloqueio() {
        assertThrows(IllegalArgumentException.class, () -> service.alterarStatus(7L, UsuarioStatus.INATIVO));
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void naoModificaUsuariosInternosPorId() {
        when(usuarios.findByIdAndPerfil(8L, Perfil.CLIENTE)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.alterarStatus(8L, UsuarioStatus.BLOQUEADO));
        assertThrows(NoSuchElementException.class, () -> service.redefinirSenha(8L, "novaSenha123"));
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void redefineSenhaDoClienteComHash() {
        Usuario cliente = usuario(9L, Perfil.CLIENTE);
        when(usuarios.findByIdAndPerfil(9L, Perfil.CLIENTE)).thenReturn(Optional.of(cliente));
        when(encoder.encode("novaSenha123")).thenReturn("bcrypt_hash");

        service.redefinirSenha(9L, "novaSenha123");

        assertEquals("bcrypt_hash", cliente.getSenha());
        verify(encoder).encode("novaSenha123");
        verify(usuarios).save(cliente);
    }

    @Test
    void rejeitaSenhaCurtaSemAlterarUsuario() {
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha(4L, "123"));
        verify(usuarios, never()).save(any(Usuario.class));
    }
}
