package cl.aroma.backend_aroma.repository;

import cl.aroma.backend_aroma.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}