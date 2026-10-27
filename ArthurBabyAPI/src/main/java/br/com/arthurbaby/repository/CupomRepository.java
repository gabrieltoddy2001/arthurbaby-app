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

    @Query("""
        SELECT c FROM Cupom c 
        WHERE c.ativo = true 
          AND (c.validoAte IS NULL OR c.validoAte >= :agora)
          AND (c.validoDe IS NULL OR c.validoDe <= :agora)
    """)
    List<Cupom> findCuponsAtivos(@Param("agora") LocalDateTime agora);
}