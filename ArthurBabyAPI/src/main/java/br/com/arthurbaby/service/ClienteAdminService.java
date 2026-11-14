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

    public ClienteAdminService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
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
        return ClienteAdminResponse.from(usuarios.save(cliente));
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        if (novaSenha == null || novaSenha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres");
        }
        Usuario cliente = buscarCliente(id);
        cliente.setSenha(encoder.encode(novaSenha));
        usuarios.save(cliente);
    }

    private Usuario buscarCliente(Long id) {
        return usuarios.findByIdAndPerfil(id, Perfil.CLIENTE)
                .orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado"));
    }
}
