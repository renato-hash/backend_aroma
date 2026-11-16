package cl.aroma.backend_aroma.controller;

import cl.aroma.backend_aroma.entity.DetallePedido;
import cl.aroma.backend_aroma.entity.Pedido;
import cl.aroma.backend_aroma.repository.PedidoRepository;
import cl.aroma.backend_aroma.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoController(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public List<java.util.Map<String, Object>> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByIdDesc();
        List<java.util.Map<String, Object>> respuesta = new ArrayList<>();

        for (Pedido p : pedidos) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("numeroOrden", p.getNumeroOrden());
            item.put("fecha", p.getFecha());
            item.put("cliente", Map.of(
                    "rut", p.getRutCliente() != null ? p.getRutCliente() : "",
                    "nombre", p.getNombreCliente() != null ? p.getNombreCliente() : "",
                    "email", p.getEmailCliente() != null ? p.getEmailCliente() : "",
                    "telefono", p.getTelefonoCliente() != null ? p.getTelefonoCliente() : ""
            ));
            item.put("despacho", Map.of(
                    "direccion", p.getDireccion() != null ? p.getDireccion() : "",
                    "comuna", p.getComuna() != null ? p.getComuna() : "",
                    "region", p.getRegion() != null ? p.getRegion() : ""
            ));
            item.put("metodoPago", p.getMetodoPago());
            item.put("productos", p.getProductos());
            item.put("total", p.getTotal());
            item.put("estado", p.getEstado());
            respuesta.add(item);
        }

        return respuesta;
    }

    @PostMapping
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> crearPedido(@RequestBody Map<String, Object> payload) {
        Map<String, Object> cliente = (Map<String, Object>) payload.getOrDefault("cliente", Map.of());
        Map<String, Object> despacho = (Map<String, Object>) payload.getOrDefault("despacho", Map.of());
        List<java.util.Map<String, Object>> items = (List<java.util.Map<String, Object>>) payload.getOrDefault("productos", List.of());

        Pedido pedido = new Pedido();
        pedido.setNumeroOrden(String.valueOf(payload.getOrDefault("numeroOrden", "ORD-" + System.currentTimeMillis())));
        pedido.setFecha(String.valueOf(payload.getOrDefault("fecha", "")));
        pedido.setRutCliente(String.valueOf(cliente.getOrDefault("rut", "")));
        pedido.setNombreCliente(String.valueOf(cliente.getOrDefault("nombre", "")));
        pedido.setEmailCliente(String.valueOf(cliente.getOrDefault("email", "")));
        pedido.setTelefonoCliente(String.valueOf(cliente.getOrDefault("telefono", "")));
        pedido.setDireccion(String.valueOf(despacho.getOrDefault("direccion", "")));
        pedido.setComuna(String.valueOf(despacho.getOrDefault("comuna", "")));
        pedido.setRegion(String.valueOf(despacho.getOrDefault("region", "")));
        pedido.setMetodoPago(String.valueOf(payload.getOrDefault("metodoPago", "")));
        pedido.setTotal(((Number) payload.getOrDefault("total", 0)).intValue());
        pedido.setEstado("Pendiente");

        List<DetallePedido> detalles = new ArrayList<>();
        for (Map<String, Object> prodMap : items) {
            DetallePedido det = new DetallePedido();
            Object idObj = prodMap.get("id");
            Long prodId = idObj instanceof Number ? ((Number) idObj).longValue() : null;

            det.setProductoId(prodId);
            det.setNombre(String.valueOf(prodMap.getOrDefault("nombre", "")));
            det.setMl(prodMap.get("ml") != null ? String.valueOf(prodMap.get("ml")) : null);
            det.setPrecio(((Number) prodMap.getOrDefault("precio", 0)).intValue());
            det.setCantidad(((Number) prodMap.getOrDefault("cantidad", 1)).intValue());
            det.setPedido(pedido);
            detalles.add(det);

            if (prodId != null) {
                productoRepository.findById(prodId).ifPresent(prod -> {
                    int nuevoStock = Math.max(0, prod.getStock() - det.getCantidad());
                    prod.setStock(nuevoStock);
                    productoRepository.save(prod);
                });
            }
        }

        pedido.setProductos(detalles);
        Pedido guardado = pedidoRepository.save(pedido);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", guardado.getId(),
                "numeroOrden", guardado.getNumeroOrden(),
                "estado", guardado.getEstado()
        ));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Estado requerido."));
        }

        return pedidoRepository.findById(id)
                .<ResponseEntity<?>>map(pedido -> {
                    pedido.setEstado(nuevoEstado);
                    pedidoRepository.save(pedido);
                    return ResponseEntity.ok(Map.of("id", pedido.getId(), "estado", pedido.getEstado()));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}