package br.com.arthurbaby.security;

import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UsuarioRepository usuarios;
    public JwtFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwtService.lerClaims(header.substring(7));
                usuarios.findByEmail(claims.getSubject())
                        .filter(Usuario::isEnabled)
                        // Recusa token emitido antes da última troca de senha ou "sair" (401 no cliente)
                        .filter(usuario -> jwtService.emitidoDepoisDasCredenciais(claims, usuario))
                        .ifPresent(usuario -> {
                            var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
