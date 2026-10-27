package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Avaliacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    /** Avaliações do produto, mais recentes primeiro; já traz o cliente (nome exibido no app) na mesma consulta. */
    @EntityGraph(attributePaths = "cliente")
    List<Avaliacao> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);

    boolean existsByClienteIdAndProdutoId(Long clienteId, Long produtoId);

    /** Média das notas do produto; 0.0 quando ainda não há avaliações (AVG de nada é NULL). */
    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM Avaliacao a WHERE a.produto.id = :produtoId")
    Double calcularMediaAvaliacoesByProdutoId(@Param("produtoId") Long produtoId);
}
