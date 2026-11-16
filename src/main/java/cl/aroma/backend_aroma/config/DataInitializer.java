package cl.aroma.backend_aroma.config;

import cl.aroma.backend_aroma.entity.Producto;
import cl.aroma.backend_aroma.entity.Usuario;
import cl.aroma.backend_aroma.repository.ProductoRepository;
import cl.aroma.backend_aroma.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializarDatos(
            UsuarioRepository usuarioRepo,
            ProductoRepository productoRepo,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (usuarioRepo.findByEmail("admin@aroma.cl").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador Principal");
                admin.setEmail("admin@aroma.cl");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRol("ROLE_ADMIN");
                usuarioRepo.save(admin);
            }

            if (usuarioRepo.findByEmail("trabajador@aroma.cl").isEmpty()) {
                Usuario operador = new Usuario();
                operador.setNombre("Operador Tienda");
                operador.setEmail("trabajador@aroma.cl");
                operador.setPassword(encoder.encode("staff123"));
                operador.setRol("ROLE_OPERADOR");
                usuarioRepo.save(operador);
            }

            if (productoRepo.count() == 0) {
                Producto p1 = new Producto();
                p1.setNombre("Bleu de Chanel EDP");
                p1.setMarca("Chanel");
                p1.setPrecio(129990);
                p1.setCategoria("Amaderados");
                p1.setStock(15);
                p1.setDescripcion("Fragancia amaderada aromática con notas cítricas y cedro.");
                p1.setImagen("/images/bluechanel.jpg");

                Producto p2 = new Producto();
                p2.setNombre("Le Beau Le Parfum");
                p2.setMarca("Jean Paul Gaultier");
                p2.setPrecio(109990);
                p2.setCategoria("Orientales");
                p2.setStock(12);
                p2.setDescripcion("Aroma intenso con madera de coco, haba tonka y sándalo.");
                p2.setImagen("/images/le-beau.webp");

                Producto p3 = new Producto();
                p3.setNombre("The Most Wanted Intense");
                p3.setMarca("Azzaro");
                p3.setPrecio(94990);
                p3.setCategoria("Gourmand");
                p3.setStock(10);
                p3.setDescripcion("Notas cálidas de cardamomo, caramelo toffee y madera ambarina.");
                p3.setImagen("/images/the-most-wanted-intense.webp");

                productoRepo.saveAll(List.of(p1, p2, p3));
            }
        };
    }
}