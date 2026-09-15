package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}
