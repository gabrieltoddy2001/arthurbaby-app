package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.entity.Enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /** Operacao atomica: logins simultaneos recebem versoes diferentes. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Usuario u set u.tokenVersion = u.tokenVersion + 1 where u.id = :id")
    int incrementarVersaoToken(@Param("id") Long id);

    @Query("select u.tokenVersion from Usuario u where u.id = :id")
    Long obterVersaoToken(@Param("id") Long id);
}
