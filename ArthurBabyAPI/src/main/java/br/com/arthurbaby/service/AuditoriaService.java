package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Auditoria;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trilha de auditoria gravada SOMENTE pelo servidor. O autor vem do token (SecurityContext), nunca do corpo
 * da requisição: o navegador não consegue criar, alterar nem apagar registros (no CRUD genérico a auditoria é
 * somente leitura e restrita ao ADMINISTRADOR).
 */
@Service
public class AuditoriaService {
    private final AuditoriaRepository auditorias;

    public AuditoriaService(AuditoriaRepository auditorias) {
        this.auditorias = auditorias;
    }

    /** Grava na mesma transação da operação: se a operação falhar, o registro também é desfeito. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrar(String acao, String entidade, Long entidadeId, String descricao) {
        Auditoria a = new Auditoria();
        Usuario logado = usuarioLogado();
        a.setUsuario(logado == null ? "Sistema" : logado.getNomeCompleto() + " (" + logado.getPerfil() + ")");
        a.setAcao(acao);
        a.setEntidade(entidade);
        a.setEntidadeId(entidadeId);
        a.setDescricao(descricao);
        auditorias.save(a);
    }

    /** Usuário autenticado pelo JwtFilter, ou null fora de uma requisição autenticada. */
    public static Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Usuario u ? u : null;
    }
}
