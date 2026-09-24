package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CupomRepository extends JpaRepository<Cupom, Long> {
    Optional<Cupom> findByCodigoIgnoreCase(String codigo);
}
