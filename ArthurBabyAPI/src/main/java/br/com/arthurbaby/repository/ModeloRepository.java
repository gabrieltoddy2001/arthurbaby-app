package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    Optional<Modelo> findByNome(String nome);
}
