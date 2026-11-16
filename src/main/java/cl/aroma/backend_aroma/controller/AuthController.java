package cl.aroma.backend_aroma.controller;

import cl.aroma.backend_aroma.entity.Usuario;
import cl.aroma.backend_aroma.repository.UsuarioRepository;
import cl.aroma.backend_aroma.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> datos) {
        String nombre = datos.getOrDefault("nombre", "").trim();
        String email = datos.getOrDefault("email", "").trim().toLowerCase();
        String password = datos.getOrDefault("password", "");

        if (nombre.length() < 3 || email.isEmpty() || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Datos de registro inválidos."));
        }

        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "El correo ya se encuentra registrado."));
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(password));
        nuevo.setRol("ROLE_CLIENTE");

        Usuario guardado = usuarioRepository.save(nuevo);
        String token = jwtService.generarToken(guardado.getEmail(), guardado.getRol());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", guardado.getId(),
                "nombre", guardado.getNombre(),
                "email", guardado.getEmail(),
                "rol", "Cliente",
                "token", token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String email = credenciales.getOrDefault("email", "").trim().toLowerCase();
        String password = credenciales.getOrDefault("password", "");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Credenciales incorrectas."));
        }

        Usuario usuario = usuarioOpt.get();
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol());

        String rolLegible = switch (usuario.getRol()) {
            case "ROLE_ADMIN" -> "admin";
            case "ROLE_OPERADOR" -> "trabajador";
            default -> "Cliente";
        };

        return ResponseEntity.ok(Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getEmail(),
                "rol", rolLegible,
                "token", token
        ));
    }
}