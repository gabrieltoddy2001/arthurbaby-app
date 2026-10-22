package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface CupomRepository extends JpaRepository<Cupom, Long> {
    Optional<Cupom> findByCodigoIgnoreCase(String codigo);

    /**
     * Cupons que podem ser usados agora: ativos, ja iniciados (validoDe) e ainda nao expirados (validoAte).
     * Datas nulas significam "sem limite" daquele lado. Os que expiram primeiro vem antes; sem data limite, por ultimo.
     */
    @Query("""
            SELECT c FROM Cupom c
            WHERE c.ativo = true
              AND (c.validoAte IS NULL OR c.validoAte >= :agora)
              AND (c.validoDe IS NULL OR c.validoDe <= :agora)
            ORDER BY CASE WHEN c.validoAte IS NULL THEN 1 ELSE 0 END, c.validoAte, c.codigo
            """)
    List<Cupom> findCuponsAtivos(@Param("agora") LocalDateTime agora);
}
