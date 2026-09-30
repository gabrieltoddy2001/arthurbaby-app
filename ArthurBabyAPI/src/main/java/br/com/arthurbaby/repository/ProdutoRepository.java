package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {
    Optional<Produto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    boolean existsBySku(String sku);
}
