package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByClienteId(Long clienteId);
    Optional<Favorito> findByClienteIdAndProdutoId(Long clienteId, Long produtoId);
}
