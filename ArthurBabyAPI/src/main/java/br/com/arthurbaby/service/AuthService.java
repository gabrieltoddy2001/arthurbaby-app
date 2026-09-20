package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import br.com.arthurbaby.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse login(LoginRequest request) {
        String login = request.login() == null ? "" : request.login().trim();
        String loginNormalizado = login.contains("@") ? login.toLowerCase(Locale.ROOT) : login;
        Usuario usuario = usuarios.findByEmail(loginNormalizado)
                .or(() -> usuarios.findByCpf(loginNormalizado.replaceAll("\\D", "")))
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        if (!encoder.matches(request.senha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha invalida");
        }
        if (!usuario.isEnabled()) {
            throw new IllegalArgumentException("Conta inativa. Entre em contato com o suporte.");
        }
        return new AuthResponse(jwt.gerarToken(usuario), usuario.getId(), usuario.getNomeCompleto(), usuario.getPerfil().name());
    }

    public AuthResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (email == null || email.isBlank()) throw new IllegalArgumentException("E-mail e obrigatorio");
        if (usuarios.existsByEmail(email)) throw new IllegalArgumentException("E-mail ja cadastrado");

        String cpf = request.cpf() == null ? null : request.cpf().replaceAll("\\D", "");
        if (cpf != null && !cpf.isBlank()) {
            if (!validarCpf(cpf)) throw new IllegalArgumentException("CPF invalido");
            if (usuarios.existsByCpf(cpf)) throw new IllegalArgumentException("CPF ja cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setTelefone(request.telefone());
        usuario.setSenha(encoder.encode(request.senha()));
        usuario.setAceiteTermoUso(request.aceiteTermoUso());
        usuario.setAceiteLgpd(request.aceiteLgpd());
        if (request.aceiteTermoUso()) usuario.setDataAceiteTermoUso(LocalDateTime.now());
        if (request.aceiteLgpd()) usuario.setDataAceiteLgpd(LocalDateTime.now());

        if (request.endereco() != null) {
            EnderecoRequest er = request.endereco();
            Endereco endereco = new Endereco();
            endereco.setUsuario(usuario);
            endereco.setCep(er.cep());
            endereco.setLogradouro(er.logradouro());
            endereco.setNumero(er.numero());
            endereco.setComplemento(er.complemento());
            endereco.setBairro(er.bairro());
            endereco.setCidade(er.cidade());
            endereco.setUf(er.uf());
            endereco.setReferencia(er.referencia());
            endereco.setPrincipal(true);
            usuario.getEnderecos().add(endereco);
        }

        usuarios.save(usuario);
        return new AuthResponse(jwt.gerarToken(usuario), usuario.getId(), usuario.getNomeCompleto(), usuario.getPerfil().name());
    }

    public void recuperarSenha(String email) {
        String normalizado = normalizarEmail(email);
        usuarios.findByEmail(normalizado).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacaoSenha(token);
            usuario.setTokenRecuperacaoSenhaExpiraEm(LocalDateTime.now().plusMinutes(30));
            usuarios.save(usuario);
            log.info("[DEV] Link de recuperacao de senha para {}: token={} (expira em 30 min)", usuario.getEmail(), token);
        });
        // Resposta identica exista ou nao o e-mail, para nao revelar quais e-mails estao cadastrados.
    }

    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token invalido");
        if (novaSenha == null || novaSenha.length() < 4) throw new IllegalArgumentException("Senha deve ter ao menos 4 caracteres");
        Usuario usuario = usuarios.findByTokenRecuperacaoSenha(token)
                .orElseThrow(() -> new NoSuchElementException("Token invalido ou expirado"));
        if (usuario.getTokenRecuperacaoSenhaExpiraEm() == null
                || usuario.getTokenRecuperacaoSenhaExpiraEm().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token invalido ou expirado");
        }
        usuario.setSenha(encoder.encode(novaSenha));
        usuario.setTokenRecuperacaoSenha(null);
        usuario.setTokenRecuperacaoSenhaExpiraEm(null);
        usuarios.save(usuario);
    }

    static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    static boolean validarCpf(String cpfDigitos) {
        if (cpfDigitos == null || cpfDigitos.length() != 11 || cpfDigitos.matches("(\\d)\\1{10}")) return false;
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += (cpfDigitos.charAt(i) - '0') * (10 - i);
            int d1 = 11 - (soma % 11);
            if (d1 >= 10) d1 = 0;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += (cpfDigitos.charAt(i) - '0') * (11 - i);
            int d2 = 11 - (soma % 11);
            if (d2 >= 10) d2 = 0;

            return (cpfDigitos.charAt(9) - '0') == d1 && (cpfDigitos.charAt(10) - '0') == d2;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
