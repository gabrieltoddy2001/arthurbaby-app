package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import br.com.arthurbaby.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    private final LimiteTentativasLogin limiteTentativas;
    /** Hash de uma senha aleatória, gerado na primeira tentativa com conta inexistente (iguala o tempo de resposta). */
    private volatile String hashFicticio;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt, LimiteTentativasLogin limiteTentativas) {
        this.limiteTentativas = limiteTentativas;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Mesma mensagem para usuário inexistente e senha errada: não revela quais e-mails/CPFs têm conta. */
    static final String LOGIN_INVALIDO = "E-mail/CPF ou senha inválidos.";

    public AuthResponse login(LoginRequest request) {
        return login(request, null);
    }

    private String hashFicticio() {
        if (hashFicticio == null) hashFicticio = encoder.encode(java.util.UUID.randomUUID().toString());
        return hashFicticio;
    }

    /** {@code ip}: endereço de quem tentou, usado no limite de tentativas (pode ser nulo). */
    public AuthResponse login(LoginRequest request, String ip) {
        String login = request.login() == null ? "" : request.login().trim();
        limiteTentativas.verificar(login, ip);
        String loginNormalizado = login.contains("@") ? login.toLowerCase(Locale.ROOT) : login;
        Usuario usuario = loginNormalizado.isEmpty() ? null : usuarios.findByEmail(loginNormalizado)
                .or(() -> usuarios.findByCpf(loginNormalizado.replaceAll("\\D", "")))
                .orElse(null);
        String senha = request.senha() == null ? "" : request.senha();
        boolean senhaConfere = encoder.matches(senha, usuario != null ? usuario.getSenha() : hashFicticio());
        if (usuario == null || !senhaConfere) {
            limiteTentativas.registrarFalha(login, ip);
            throw new IllegalArgumentException(LOGIN_INVALIDO);
        }
        limiteTentativas.registrarSucesso(login);
        // Só depois da senha certa: informar que a conta está inativa não revela nada a quem não sabe a senha
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

        PoliticaSenha.validar(request.senha(), email, cpf);

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

    @Transactional
    public void alterarSenha(Long usuarioId, AlterarSenhaRequest request) {
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado"));

        if (!encoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }
        PoliticaSenha.validar(request.novaSenha(), usuario.getEmail(), usuario.getCpf());

        usuario.setSenha(encoder.encode(request.novaSenha()));
        usuario.invalidarSessoes(); // tokens emitidos antes da troca deixam de valer
        usuarios.save(usuario);
    }

    /** "Sair": invalida todos os tokens já emitidos para o usuário (em qualquer aparelho). */
    @Transactional
    public void logout(Long usuarioId) {
        usuarios.findById(usuarioId).ifPresent(usuario -> {
            usuario.invalidarSessoes();
            usuarios.save(usuario);
        });
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