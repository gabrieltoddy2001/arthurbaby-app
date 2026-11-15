package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.LoginRequest;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.repository.UsuarioRepository;
import br.com.arthurbaby.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceSessaoTest {
    @Test
    void loginIncrementaVersaoAntesDeGerarToken() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        Usuario u = new Usuario();
        u.setId(7L); u.setEmail("cliente@example.com"); u.setNomeCompleto("Cliente Teste");
        u.setSenha("hash"); u.setPerfil(Perfil.CLIENTE);
        when(usuarios.findByEmail("cliente@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("senha", "hash")).thenReturn(true);
        when(usuarios.incrementarVersaoToken(7L)).thenReturn(1);
        when(usuarios.obterVersaoToken(7L)).thenReturn(12L);
        when(jwt.gerarToken(u)).thenReturn("token-novo");
        var resposta = new AuthService(usuarios, encoder, jwt)
                .login(new LoginRequest("cliente@example.com", "senha"));
        assertEquals("token-novo", resposta.token());
        assertEquals(Long.valueOf(12L), u.getTokenVersion());
        var ordem = inOrder(usuarios, jwt);
        ordem.verify(usuarios).incrementarVersaoToken(7L);
        ordem.verify(usuarios).obterVersaoToken(7L);
        ordem.verify(jwt).gerarToken(u);
    }

    @Test
    void senhaErradaNaoInvalidaSessaoAnterior() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        Usuario u = new Usuario();
        u.setId(7L); u.setEmail("cliente@example.com"); u.setSenha("hash");
        when(usuarios.findByEmail("cliente@example.com")).thenReturn(Optional.of(u));
        when(encoder.matches("errada", "hash")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () ->
                new AuthService(usuarios, encoder, jwt).login(new LoginRequest("cliente@example.com", "errada")));
        verify(usuarios, never()).incrementarVersaoToken(anyLong());
        verifyNoInteractions(jwt);
    }
}
