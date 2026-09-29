package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.PasswordResetToken;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.PasswordResetTokenRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {
    private UsuarioRepository usuarios;
    private PasswordResetTokenRepository tokens;
    private PasswordEncoder encoder;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        tokens = mock(PasswordResetTokenRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new PasswordResetService(usuarios, tokens, encoder);
    }

    @Test
    void solicitarRecuperacaoGeraTokenDeUmaHoraSemAlterarSenha() {
        Usuario usuario = usuario();
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(tokens.findByUsuarioIdAndUsadoFalse(7L)).thenReturn(List.of());

        service.solicitarRecuperacao(" ANA@EXAMPLE.COM ");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).save(captor.capture());
        PasswordResetToken prt = captor.getValue();
        assertNotNull(prt.getToken());
        assertSame(usuario, prt.getUsuario());
        assertTrue(prt.getExpiraEm().isAfter(LocalDateTime.now().plusMinutes(59)));
        assertTrue(prt.getExpiraEm().isBefore(LocalDateTime.now().plusMinutes(61)));
        assertEquals("hash", usuario.getSenha());
        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    void novaSolicitacaoInvalidaTokensPendentes() {
        Usuario usuario = usuario();
        PasswordResetToken antigo = token("antigo", usuario, LocalDateTime.now().plusMinutes(30));
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(tokens.findByUsuarioIdAndUsadoFalse(7L)).thenReturn(List.of(antigo));

        service.solicitarRecuperacao("ana@example.com");

        assertTrue(antigo.isUsado());
    }

    @Test
    void solicitarRecuperacaoNaoFazNadaQuandoEmailNaoExiste() {
        when(usuarios.findByEmail("inexistente@example.com")).thenReturn(Optional.empty());

        service.solicitarRecuperacao("inexistente@example.com");

        verifyNoInteractions(tokens);
    }

    @Test
    void redefinirSenhaCodificaSenhaEMarcaTokenComoUsado() {
        Usuario usuario = usuario();
        PasswordResetToken prt = token("token-valido", usuario, LocalDateTime.now().plusMinutes(5));
        when(tokens.findByToken("token-valido")).thenReturn(Optional.of(prt));
        when(encoder.encode("nova-senha")).thenReturn("novo-hash");

        service.redefinirSenha("token-valido", "nova-senha");

        assertEquals("novo-hash", usuario.getSenha());
        assertTrue(prt.isUsado());
        verify(usuarios).save(usuario);
        verify(tokens).save(prt);
    }

    @Test
    void redefinirSenhaRejeitaTokenInvalidoUsadoExpiradoESenhaCurta() {
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha(" ", "senha"));
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha("token", "abc"));

        when(tokens.findByToken("inexistente")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha("inexistente", "senha"));

        PasswordResetToken usado = token("usado", usuario(), LocalDateTime.now().plusMinutes(5));
        usado.setUsado(true);
        when(tokens.findByToken("usado")).thenReturn(Optional.of(usado));
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha("usado", "senha"));

        PasswordResetToken expirado = token("expirado", usuario(), LocalDateTime.now().minusSeconds(1));
        when(tokens.findByToken("expirado")).thenReturn(Optional.of(expirado));
        assertThrows(IllegalArgumentException.class, () -> service.redefinirSenha("expirado", "senha"));

        verify(usuarios, never()).save(any(Usuario.class));
    }

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("ana@example.com");
        usuario.setSenha("hash");
        return usuario;
    }

    private static PasswordResetToken token(String valor, Usuario usuario, LocalDateTime expiraEm) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(valor);
        prt.setUsuario(usuario);
        prt.setExpiraEm(expiraEm);
        return prt;
    }
}
