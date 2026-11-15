package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.entity.Enums.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SEGREDO = "chave-de-teste-arthurbaby-segura-com-pelo-menos-32-bytes-123";
    private JwtService jwt;

    @BeforeEach
    void configurar() {
        jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", SEGREDO);
        ReflectionTestUtils.setField(jwt, "expirationMinutes", 60L);
    }

    private Usuario usuario(long id, long versao, String hash) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail("teste" + id + "@example.com");
        u.setSenha(hash);
        u.setPerfil(Perfil.CLIENTE);
        u.setTokenVersion(versao);
        return u;
    }

    @Test
    void tokenAtualPodeAutenticar() {
        Usuario u = usuario(3, 8, "hash-bcrypt-1");
        Claims claims = jwt.lerClaims(jwt.gerarToken(u));
        assertTrue(jwt.validoParaUsuario(claims, u));
    }

    @Test
    void tokenAntigoDeixaDeValerAposNovoLogin() {
        Usuario u = usuario(3, 8, "hash-bcrypt-1");
        Claims antigo = jwt.lerClaims(jwt.gerarToken(u));
        u.setTokenVersion(9L);
        assertFalse(jwt.validoParaUsuario(antigo, u));
        assertTrue(jwt.validoParaUsuario(jwt.lerClaims(jwt.gerarToken(u)), u));
    }

    @Test
    void tokenAntigoDeixaDeValerAposQualquerTrocaDeSenha() {
        Usuario u = usuario(3, 8, "hash-bcrypt-antigo");
        Claims antigo = jwt.lerClaims(jwt.gerarToken(u));
        u.setSenha("hash-bcrypt-novo");
        assertFalse(jwt.validoParaUsuario(antigo, u));
        assertTrue(jwt.validoParaUsuario(jwt.lerClaims(jwt.gerarToken(u)), u));
    }

    @Test
    void tokenDeOutroUsuarioNaoAutentica() {
        Usuario primeiro = usuario(3, 8, "hash-bcrypt-1");
        Usuario segundo = usuario(4, 8, "hash-bcrypt-1");
        assertFalse(jwt.validoParaUsuario(jwt.lerClaims(jwt.gerarToken(primeiro)), segundo));
    }

    @Test
    void tokensAntigosSemVersaoNaoSaoAceitos() {
        Usuario u = usuario(3, 0, "hash-bcrypt-1");
        String legado = Jwts.builder().subject(u.getEmail())
                .claim("id", u.getId()).claim("perfil", u.getPerfil().name())
                .expiration(new Date(System.currentTimeMillis() + 3600_000L))
                .signWith(Keys.hmacShaKeyFor(SEGREDO.getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertFalse(jwt.validoParaUsuario(jwt.lerClaims(legado), u));
    }
}
