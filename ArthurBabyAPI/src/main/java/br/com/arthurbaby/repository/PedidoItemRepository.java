package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {}
