package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import br.com.arthurbaby.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmail(request.login())
                .or(() -> usuarios.findByCpf(request.login()))
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        if (!encoder.matches(request.senha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha invalida");
        }
        return new AuthResponse(jwt.gerarToken(usuario), usuario.getId(), usuario.getNomeCompleto(), usuario.getPerfil().name());
    }
    public AuthResponse cadastrar(CadastroRequest request) {
        if (usuarios.existsByEmail(request.email())) throw new IllegalArgumentException("E-mail ja cadastrado");
        if (request.cpf() != null && usuarios.existsByCpf(request.cpf())) throw new IllegalArgumentException("CPF ja cadastrado");
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setEmail(request.email());
        usuario.setCpf(request.cpf());
        usuario.setTelefone(request.telefone());
        usuario.setSenha(encoder.encode(request.senha()));
        usuario.setAceiteTermoUso(request.aceiteTermoUso());
        usuario.setAceiteLgpd(request.aceiteLgpd());
        if (request.aceiteTermoUso()) usuario.setDataAceiteTermoUso(LocalDateTime.now());
        if (request.aceiteLgpd()) usuario.setDataAceiteLgpd(LocalDateTime.now());
        usuarios.save(usuario);
        return new AuthResponse(jwt.gerarToken(usuario), usuario.getId(), usuario.getNomeCompleto(), usuario.getPerfil().name());
    }
}
