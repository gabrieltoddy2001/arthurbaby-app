package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.entity.Enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /** Somente clientes, com endereços para os detalhes do painel. */
    @EntityGraph(attributePaths = "enderecos")
    List<Usuario> findDistinctByPerfil(Perfil perfil);
    /** Restringe operações da aba Clientes a perfis CLIENTE no banco. */
    Optional<Usuario> findByIdAndPerfil(Long id, Perfil perfil);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
