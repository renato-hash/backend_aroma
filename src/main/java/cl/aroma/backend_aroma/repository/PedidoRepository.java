package cl.aroma.backend_aroma.repository;

import cl.aroma.backend_aroma.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByIdDesc();
}