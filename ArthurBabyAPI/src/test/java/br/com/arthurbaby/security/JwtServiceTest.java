package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String SECRET = "01234567890123456789012345678901";
    private JwtService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", SECRET);
        ReflectionTestUtils.setField(service, "expirationMinutes", 30L);

        usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("cliente@example.com");
        usuario.setPerfil(Perfil.VENDEDOR);
    }

    @Test
    void gerarTokenAssinaClaimsDeUsuarioEExpiracao() {
        String token = service.gerarToken(usuario);
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("cliente@example.com", claims.getSubject());
        assertEquals(42L, ((Number) claims.get("id")).longValue());
        assertEquals("VENDEDOR", claims.get("perfil"));
        assertTrue(claims.getExpiration().after(Date.from(Instant.now())));
        assertEquals("cliente@example.com", service.getSubject(token));
    }

    @Test
    void getSubjectRejeitaAssinaturaAlteradaETokenMalformado() {
        String token = service.gerarToken(usuario);
        String[] partes = token.split("\\.");
        byte[] assinatura = Base64.getUrlDecoder().decode(partes[2]);
        assinatura[0] ^= 1;
        partes[2] = Base64.getUrlEncoder().withoutPadding().encodeToString(assinatura);

        assertThrows(JwtException.class, () -> service.getSubject(String.join(".", partes)));
        assertThrows(JwtException.class, () -> service.getSubject("token-malformado"));
    }
}
