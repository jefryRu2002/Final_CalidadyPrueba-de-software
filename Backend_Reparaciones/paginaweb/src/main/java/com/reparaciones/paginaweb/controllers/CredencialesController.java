package com.reparaciones.paginaweb.controllers;

import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.services.AuthService;
import com.reparaciones.paginaweb.services.PersonaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class CredencialesController {

    private final AuthService authService;
    private final PersonaService personaService;

    public CredencialesController(AuthService authService,
                                  PersonaService personaService) {
        this.authService = authService;
        this.personaService = personaService;
    }

    // ==================== LOGIN ====================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String usuario = credentials.get("usuario");
        String contrasena = credentials.get("contrasena");

        if (usuario == null || usuario.trim().isEmpty()
                || contrasena == null || contrasena.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Usuario y contraseña son obligatorios"));
        }

        Map<String, Object> respuesta = authService.login(usuario.trim(), contrasena);
        return ResponseEntity.ok(respuesta);
    }

    // ==================== REGISTRO PÚBLICO (siempre CLIENTE) ====================
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> datos) {
        return registrarConRol(datos, "CLIENTE");
    }

    // ==================== CREAR USUARIO POR ADMIN ====================
    @PostMapping("/admin/crear-usuario")
    public ResponseEntity<Map<String, Object>> crearUsuarioAdmin(@RequestBody Map<String, String> datos) {
        String rol = datos.get("rol");
        if (rol == null || rol.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol es obligatorio"));
        }

        String rolNormalizado = rol.trim().toUpperCase();
        if (rolNormalizado.equals("TÉCNICO")) rolNormalizado = "TECNICO";
        if (rolNormalizado.equals("ADMIN")) rolNormalizado = "ADMINISTRADOR";

        if (!rolNormalizado.equals("CLIENTE")
                && !rolNormalizado.equals("TECNICO")
                && !rolNormalizado.equals("ADMINISTRADOR")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Rol no válido. Use: CLIENTE, TECNICO, ADMINISTRADOR"));
        }

        return registrarConRol(datos, rolNormalizado);
    }

    // ==================== MÉTODO COMÚN DE REGISTRO ====================
    private ResponseEntity<Map<String, Object>> registrarConRol(Map<String, String> datos, String rol) {
        String dniStr = datos.get("dni");
        String nombre = datos.get("nombre");
        String apellido = datos.get("apellido");
        String telefono = datos.getOrDefault("telefono", "");
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");
        String fechaNacimiento = datos.get("fechaNacimiento");

        // Validaciones
        if (dniStr == null || dniStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El DNI es obligatorio"));
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo/usuario es obligatorio"));
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es obligatoria"));
        }
        if (personaService.existsByDni(dniStr.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El DNI ya está registrado"));
        }

        try {
            PersonaModel persona = new PersonaModel();
            persona.setDni(dniStr.trim());
            persona.setNombre(nombre != null ? nombre.trim() : "");
            persona.setApellido(apellido != null ? apellido.trim() : "");
            persona.setTelefono(telefono != null ? telefono.trim() : "");
            persona.setCorreo(usuario.trim());
            if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                persona.setFechaNacimiento(fechaNacimiento);
            }

            // AuthService valida duplicados de usuario, hashea y guarda
            Map<String, Object> resultado = authService.registrarUsuario(
                    persona,
                    usuario.trim().toLowerCase(),
                    contrasena,
                    rol
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar: " + e.getMessage()));
        }
    }

    // ==================== OLVIDO CONTRASEÑA ====================
    @PostMapping("/olvide-contrasena")
    public ResponseEntity<Map<String, Object>> olvideContrasena(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");

        if (correo == null || correo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
        }

        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, recibirás instrucciones para restablecer tu contraseña"
        ));
    }
}