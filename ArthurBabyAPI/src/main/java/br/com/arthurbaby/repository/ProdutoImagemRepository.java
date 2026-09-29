package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Long> {
    List<ProdutoImagem> findByProdutoIdOrderByOrdemExibicaoAsc(Long produtoId);
}
