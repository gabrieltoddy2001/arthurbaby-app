package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteIdOrderByCriadoEmDesc(Long clienteId);
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
}
