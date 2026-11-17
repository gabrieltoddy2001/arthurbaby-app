package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret:}") private String secret;
    /** Duração do token de CLIENTE (app). */
    @Value("${app.jwt.expiration-minutes}") private long expirationMinutes;
    /** Duração menor para ADMINISTRADOR e VENDEDOR (painel), que têm acesso a dados de todos. */
    @Value("${app.jwt.expiration-minutes-equipe:120}") private long expirationMinutesEquipe;
    private SecretKey chave;

    /**
     * A chave vem de JWT_SECRET (variavel de ambiente ou application-local.properties). Sem ela, ou se for curta
     * demais, gera uma chave aleatoria forte: os tokens passam a valer so ate a API reiniciar, mas nenhuma chave
     * fixa fica no codigo-fonte.
     */
    @PostConstruct
    void iniciarChave() {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = new byte[64];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET ausente ou com menos de 32 caracteres: usando chave aleatoria (tokens invalidados a cada reinicio).");
        }
        chave = Keys.hmacShaKeyFor(bytes);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                // "iat" do JWT só guarda segundos; com milissegundos, um "sair"/troca de senha no mesmo segundo também invalida
                .claim("iatMs", agora.toEpochMilli())
                .expiration(Date.from(agora.plusSeconds(minutosDeValidade(usuario) * 60)))
                .signWith(chave)
                .compact();
    }

    private long minutosDeValidade(Usuario usuario) {
        return usuario.getPerfil() == br.com.arthurbaby.entity.Enums.Perfil.CLIENTE ? expirationMinutes : expirationMinutesEquipe;
    }

    /** Valida assinatura e expiração e devolve os dados do token (lança exceção se inválido). */
    public io.jsonwebtoken.Claims lerClaims(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Token emitido antes da última troca de senha / "sair" do usuário não vale mais.
     * Usa o horário em milissegundos (claim "iatMs"); tokens antigos, sem ele, usam o "iat" (segundos).
     */
    public boolean emitidoDepoisDasCredenciais(io.jsonwebtoken.Claims claims, Usuario usuario) {
        if (usuario.getCredenciaisAlteradasEm() == null) return true;
        Long emitidoMs = claims.get("iatMs", Long.class);
        if (emitidoMs == null && claims.getIssuedAt() != null) emitidoMs = claims.getIssuedAt().getTime();
        if (emitidoMs == null) return false;
        long marcoMs = usuario.getCredenciaisAlteradasEm().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return emitidoMs > marcoMs;
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
