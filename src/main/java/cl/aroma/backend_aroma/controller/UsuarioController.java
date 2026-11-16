package cl.aroma.backend_aroma.controller;

import cl.aroma.backend_aroma.entity.Usuario;
import cl.aroma.backend_aroma.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<java.util.Map<String, Object>> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "nombre", u.getNombre(),
                        "email", u.getEmail(),
                        "rol", switch (u.getRol()) {
                            case "ROLE_ADMIN" -> "Administrador";
                            case "ROLE_OPERADOR" -> "Operador";
                            default -> "Cliente";
                        }
                ))
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, String> datos) {
        String nombre = datos.getOrDefault("nombre", "").trim();
        String email = datos.getOrDefault("email", "").trim().toLowerCase();
        String rolInput = datos.getOrDefault("rol", "Cliente");
        String password = datos.getOrDefault("password", "Aroma2026!");

        if (nombre.length() < 3 || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Nombre y correo obligatorios."));
        }
        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "Correo duplicado."));
        }

        String rolSpring = switch (rolInput) {
            case "Administrador" -> "ROLE_ADMIN";
            case "Operador" -> "ROLE_OPERADOR";
            default -> "ROLE_CLIENTE";
        };

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rolSpring);

        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", guardado.getId(),
                "nombre", guardado.getNombre(),
                "email", guardado.getEmail(),
                "rol", rolInput
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    if ("admin@aroma.cl".equalsIgnoreCase(u.getEmail())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("mensaje", "No se puede eliminar al administrador principal."));
                    }
                    usuarioRepository.delete(u);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}