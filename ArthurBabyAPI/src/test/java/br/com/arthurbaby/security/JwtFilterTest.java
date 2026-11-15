package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {
    private JwtService jwt;
    private UsuarioRepository usuarios;
    private JwtFilter filtro;
    private Usuario usuario;

    @BeforeEach
    void configurar() {
        jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", "chave-de-teste-arthurbaby-segura-com-pelo-menos-32-bytes-123");
        ReflectionTestUtils.setField(jwt, "expirationMinutes", 60L);
        usuarios = mock(UsuarioRepository.class);
        filtro = new JwtFilter(jwt, usuarios);
        usuario = new Usuario();
        usuario.setId(3L); usuario.setEmail("teste@example.com");
        usuario.setPerfil(Perfil.CLIENTE); usuario.setSenha("hash-1");
        usuario.setTokenVersion(1L);
        when(usuarios.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpar() { SecurityContextHolder.clearContext(); }

    private void requisicao(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/me");
        request.addHeader("Authorization", "Bearer " + token);
        filtro.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    void jwtAtualAutentica() throws Exception {
        requisicao(jwt.gerarToken(usuario));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void novoLoginRevogaJwtAnteriorNoFiltro() throws Exception {
        String antigo = jwt.gerarToken(usuario);
        usuario.setTokenVersion(2L);
        requisicao(antigo);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void trocarSenhaRevogaJwtAnteriorNoFiltro() throws Exception {
        String antigo = jwt.gerarToken(usuario);
        usuario.setSenha("hash-2");
        requisicao(antigo);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
