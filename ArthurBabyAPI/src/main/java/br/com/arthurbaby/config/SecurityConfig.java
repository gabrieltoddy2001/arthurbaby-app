package br.com.arthurbaby.config;

import br.com.arthurbaby.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                // Usa as regras do CorsConfig tambem nas respostas geradas pelo Security (401/403)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // /me e /alterar-senha ficam antes da liberacao de /api/auth/** porque exigem token
                        .requestMatchers("/api/auth/me", "/api/auth/alterar-senha", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/h2-console/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cupons/validar").permitAll()
                        // "Meus Cupons": lista publica de cupons vigentes, consultada antes do checkout
                        .requestMatchers(HttpMethod.GET, "/api/cupons/ativos").permitAll()
                        // Histórico de movimentações (saldos, fornecedor, responsável) não é público: só a equipe da loja.
                        // Precisa vir antes da liberação de /api/estoque/** (o app não usa estas rotas).
                        .requestMatchers(HttpMethod.GET, "/api/estoque/*/movimentacoes").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/produtos", "/api/produtos/**", "/api/estoque/**").permitAll()
                        // Áreas exclusivas do ADMINISTRADOR. O menu do painel só esconde as telas; a proteção real é esta.
                        .requestMatchers("/api/admin/usuarios/**", "/api/admin/configuracoes-loja/**", "/api/admin/auditoria/**",
                                "/api/admin/enderecos/**", "/api/admin/favoritos/**").hasRole("ADMINISTRADOR")
                        // Exclusão física só pelo ADMINISTRADOR (no dia a dia, prefira desativar pelo status)
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRADOR", "VENDEDOR")
                        .anyRequest().authenticated())
                // Erros de segurança no mesmo formato JSON do ApiExceptionHandler: { "erro": ..., "codigo": ... }
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> escreverErro(response, HttpStatus.UNAUTHORIZED,
                                "Nao autenticado: token ausente, invalido ou expirado"))
                        .accessDeniedHandler((request, response, e) -> escreverErro(response, HttpStatus.FORBIDDEN,
                                "Acesso negado: seu perfil nao tem permissao para este recurso")))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static void escreverErro(HttpServletResponse response, HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\",\"codigo\":" + status.value() + "}");
    }
}
