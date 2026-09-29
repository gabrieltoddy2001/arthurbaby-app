package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.AuthResponse;
import br.com.arthurbaby.dto.CadastroRequest;
import br.com.arthurbaby.dto.LoginRequest;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import br.com.arthurbaby.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

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

class AuthServiceTest {
    private UsuarioRepository usuarios;
    private PasswordEncoder encoder;
    private JwtService jwt;
    private AuthService service;

    @BeforeEach
    void setUp() {
        usuarios = mock(UsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtService.class);
        service = new AuthService(usuarios, encoder, jwt);
    }

    @Test
    void cadastrarNormalizaEmailCodificaSenhaESalvaUsuario() {
        when(usuarios.existsByEmail("ana@example.com")).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(7L);
            return salvo;
        });
        when(encoder.encode("senha-forte")).thenReturn("senha-hash");
        when(jwt.gerarToken(any(Usuario.class))).thenReturn("jwt-token");

        AuthResponse response = service.cadastrar(new CadastroRequest(
                "Ana Lima", "  ANA@EXAMPLE.COM ", null, "11999990000", "senha-forte", true, true, null));

        assertEquals("jwt-token", response.token());
        assertEquals(7L, response.usuarioId());
        assertEquals("Ana Lima", response.nome());
        verify(encoder).encode("senha-forte");
        verify(usuarios).save(any(Usuario.class));
    }

    @Test
    void cadastrarRejeitaEmailDuplicadoAntesDeCodificarSenha() {
        when(usuarios.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(new CadastroRequest(
                "Ana Lima", " ANA@example.com ", null, null, "senha", false, false, null)));

        verify(usuarios, never()).save(any(Usuario.class));
        verify(encoder, never()).encode(any());
    }

    @Test
    void loginNormalizaEmailEDevolveToken() {
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("senha", "hash")).thenReturn(true);
        when(jwt.gerarToken(usuario)).thenReturn("jwt-token");

        AuthResponse response = service.login(new LoginRequest(" ANA@EXAMPLE.COM ", "senha"));

        assertEquals("jwt-token", response.token());
        assertEquals(7L, response.usuarioId());
        assertEquals("Ana Lima", response.nome());
        assertEquals(Perfil.CLIENTE.name(), response.perfil());
        verify(usuarios).findByEmail("ana@example.com");
    }

    @Test
    void loginRejeitaSenhaIncorretaEContaInativa() {
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("errada", "hash")).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> service.login(new LoginRequest("ana@example.com", "errada")));

        when(encoder.matches("senha", "hash")).thenReturn(true);
        usuario.setStatus(UsuarioStatus.INATIVO);
        assertThrows(IllegalArgumentException.class,
                () -> service.login(new LoginRequest("ana@example.com", "senha")));
        verify(jwt, never()).gerarToken(any(Usuario.class));
    }

    @Test
    void loginPorCpfIgnoraMascara() {
        Usuario usuario = usuarioAtivo();
        when(usuarios.findByEmail("529.982.247-25")).thenReturn(Optional.empty());
        when(usuarios.findByCpf("52998224725")).thenReturn(Optional.of(usuario));
        when(encoder.matches("senha", "hash")).thenReturn(true);
        when(jwt.gerarToken(usuario)).thenReturn("jwt-token");

        assertEquals("jwt-token", service.login(new LoginRequest(" 529.982.247-25 ", "senha")).token());
    }

    @Test
    void validarCpfAceitaDigitosValidosERejeitaSequenciasInvalidas() {
        // Known-valid fixture exercises both CPF check digits.
        assertTrue(AuthService.validarCpf("52998224725"));
        assertFalse(AuthService.validarCpf("11111111111"));
        assertFalse(AuthService.validarCpf("123"));
        assertFalse(AuthService.validarCpf(null));
    }

    private static Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNomeCompleto("Ana Lima");
        usuario.setEmail("ana@example.com");
        usuario.setSenha("hash");
        return usuario;
    }
}
