package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.ClienteAdminResponse;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.UsuarioStatus;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** Regras da aba Clientes: nenhum usuário interno pode ser listado ou alterado aqui. */
@Service
public class ClienteAdminService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public ClienteAdminService(UsuarioRepository usuarios, PasswordEncoder encoder, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<ClienteAdminResponse> listar() {
        return usuarios.findDistinctByPerfil(Perfil.CLIENTE).stream()
                // Defesa adicional se alguma consulta for alterada futuramente.
                .filter(usuario -> usuario.getPerfil() == Perfil.CLIENTE)
                .map(ClienteAdminResponse::from)
                .toList();
    }

    @Transactional
    public ClienteAdminResponse alterarStatus(Long id, UsuarioStatus status) {
        if (status != UsuarioStatus.ATIVO && status != UsuarioStatus.BLOQUEADO) {
            throw new IllegalArgumentException("Status permitido nesta acao: ATIVO ou BLOQUEADO");
        }
        Usuario cliente = buscarCliente(id);
        cliente.setStatus(status);
        ClienteAdminResponse resposta = ClienteAdminResponse.from(usuarios.save(cliente));
        auditoria.registrar(status == UsuarioStatus.BLOQUEADO ? "BLOQUEAR" : "DESBLOQUEAR", "Usuario", id,
                (status == UsuarioStatus.BLOQUEADO ? "Bloqueou" : "Desbloqueou") + " o cliente " + cliente.getNomeCompleto() + " (" + cliente.getEmail() + ")");
        return resposta;
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        PoliticaSenha.validar(novaSenha);
        Usuario cliente = buscarCliente(id);
        cliente.setSenha(encoder.encode(novaSenha));
        cliente.invalidarSessoes(); // derruba as sessões abertas com a senha antiga
        usuarios.save(cliente);
        auditoria.registrar("RESETAR_SENHA", "Usuario", id,
                "Redefiniu a senha do cliente " + cliente.getNomeCompleto() + " (" + cliente.getEmail() + ")");
    }

    private Usuario buscarCliente(Long id) {
        return usuarios.findByIdAndPerfil(id, Perfil.CLIENTE)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado"));
    }
}