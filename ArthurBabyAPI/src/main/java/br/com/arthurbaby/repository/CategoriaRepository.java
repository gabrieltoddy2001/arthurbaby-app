package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.Enums.CategoriaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByCategoriaPaiId(Long categoriaPaiId);
    List<Categoria> findByCategoriaPaiIsNullAndStatusOrderByOrdemExibicaoAsc(CategoriaStatus status);
}
