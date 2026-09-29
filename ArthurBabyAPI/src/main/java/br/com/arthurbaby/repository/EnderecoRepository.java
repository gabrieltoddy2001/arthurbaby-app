package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findByUsuarioId(Long usuarioId);
    Optional<Endereco> findByIdAndUsuarioId(Long id, Long usuarioId);
}
