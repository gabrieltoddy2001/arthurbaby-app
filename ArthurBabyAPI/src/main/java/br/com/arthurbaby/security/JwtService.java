package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Service
public class JwtService {
    @Value("${app.jwt.secret}") private String secret;
    @Value("${app.jwt.expiration-minutes}") private long expirationMinutes;

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("perfil", usuario.getPerfil().name())
                // Cada login incrementa sv; toda troca de senha altera automaticamente pv.
                // O hash BCrypt nao e incluido no JWT: pv usa HMAC com a chave do servidor.
                .claim("sv", usuario.getTokenVersion())
                .claim("pv", assinaturaSenha(usuario.getSenha()))
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expirationMinutes * 60)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return lerClaims(token).getSubject();
    }

    /** Token antigo, token de outra conta ou emitido antes de trocar a senha e rejeitado. */
    public boolean validoParaUsuario(Claims claims, Usuario usuario) {
        if (claims == null || usuario == null || usuario.getId() == null) return false;
        Object id = claims.get("id");
        Object versao = claims.get("sv");
        Object provaSenha = claims.get("pv");
        if (!(id instanceof Number idNumero) || !(versao instanceof Number versaoNumero)
                || !(provaSenha instanceof String prova)) return false;
        if (!Objects.equals(claims.getSubject(), usuario.getEmail())) return false;
        if (idNumero.longValue() != usuario.getId()) return false;
        if (usuario.getTokenVersion() == null || versaoNumero.longValue() != usuario.getTokenVersion()) return false;
        return MessageDigest.isEqual(
                prova.getBytes(StandardCharsets.US_ASCII),
                assinaturaSenha(usuario.getSenha()).getBytes(StandardCharsets.US_ASCII));
    }

    private String assinaturaSenha(String senhaCodificada) {
        if (senhaCodificada == null) throw new IllegalStateException("Hash da senha ausente");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                    mac.doFinal(senhaCodificada.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao validar a sessao", e);
        }
    }
}
