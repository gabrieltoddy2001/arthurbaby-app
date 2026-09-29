package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.PasswordResetToken;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.PasswordResetTokenRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Recuperacao de senha com token descartavel (tabela password_reset_token).
 * O token nunca volta na resposta HTTP: em producao iria por e-mail; em dev e impresso no log.
 */
@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    static final int VALIDADE_HORAS = 1;
    static final String LINK_REDEFINICAO = "https://arthurbaby.com/redefinir?token=";

    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;

    public PasswordResetService(UsuarioRepository usuarios, PasswordResetTokenRepository tokens, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
    }

    /** Nao revela se o e-mail existe: o chamador responde sempre a mesma mensagem. */
    @Transactional
    public void solicitarRecuperacao(String email) {
        String normalizado = AuthService.normalizarEmail(email);
        if (normalizado == null || normalizado.isBlank()) return;
        usuarios.findByEmail(normalizado).ifPresent(usuario -> {
            invalidarTokensPendentes(usuario);
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(UUID.randomUUID().toString());
            prt.setUsuario(usuario);
            prt.setExpiraEm(LocalDateTime.now().plusHours(VALIDADE_HORAS));
            tokens.save(prt);
            log.info("[RECUPERAR-SENHA][DEV] email={} token={} link={}{} (expira em {}h)",
                    usuario.getEmail(), prt.getToken(), LINK_REDEFINICAO, prt.getToken(), VALIDADE_HORAS);
        });
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token invalido");
        if (novaSenha == null || novaSenha.length() < 4) throw new IllegalArgumentException("Senha deve ter ao menos 4 caracteres");
        PasswordResetToken prt = tokens.findByToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("Token invalido"));
        if (prt.isUsado()) throw new IllegalArgumentException("Token ja foi usado");
        if (prt.isExpirado()) throw new IllegalArgumentException("Token expirado");

        Usuario usuario = prt.getUsuario();
        usuario.setSenha(encoder.encode(novaSenha));
        usuarios.save(usuario);

        prt.setUsado(true);
        tokens.save(prt);
    }

    /** Um novo pedido de recuperacao anula os tokens anteriores ainda nao usados. */
    private void invalidarTokensPendentes(Usuario usuario) {
        List<PasswordResetToken> pendentes = tokens.findByUsuarioIdAndUsadoFalse(usuario.getId());
        if (pendentes.isEmpty()) return;
        pendentes.forEach(t -> t.setUsado(true));
        tokens.saveAll(pendentes);
    }
}
