package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.ProdutoVariacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProdutoVariacaoRepository extends JpaRepository<ProdutoVariacao, Long> {
    List<ProdutoVariacao> findByProdutoId(Long produtoId);
}
