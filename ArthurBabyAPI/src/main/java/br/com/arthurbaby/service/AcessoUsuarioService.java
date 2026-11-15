package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Regra única de acesso a dados de um usuário (perfil, endereços, favoritos, pedidos):
 * <ul>
 *   <li>qualquer usuário acessa os próprios dados;</li>
 *   <li>ADMINISTRADOR acessa qualquer usuário;</li>
 *   <li>VENDEDOR acessa somente contas CLIENTE (nunca outro vendedor ou um administrador);</li>
 *   <li>CLIENTE nunca acessa dados de outra pessoa.</li>
 * </ul>
 * Para o CLIENTE o comportamento é idêntico ao anterior (o app mobile não é afetado).
 */
@Service
public class AcessoUsuarioService {
    private final UsuarioRepository usuarios;

    public AcessoUsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public void verificar(Long alvoId, Usuario logado, String mensagem) {
        if (logado == null) throw new AccessDeniedException(mensagem);
        if (logado.getId().equals(alvoId)) return;
        if (logado.getPerfil() == Perfil.ADMINISTRADOR) return;
        if (logado.getPerfil() == Perfil.VENDEDOR && alvoEhCliente(alvoId)) return;
        throw new AccessDeniedException(mensagem);
    }

    /** Alvo inexistente conta como cliente para o serviço responder 404 normalmente (sem revelar perfis). */
    private boolean alvoEhCliente(Long alvoId) {
        return alvoId != null && usuarios.findById(alvoId)
                .map(u -> u.getPerfil() == Perfil.CLIENTE)
                .orElse(true);
    }
}
