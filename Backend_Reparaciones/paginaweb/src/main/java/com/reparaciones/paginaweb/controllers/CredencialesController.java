package com.reparaciones.paginaweb.controllers;

import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class CredencialesController {

    private final AuthService authService;

    public CredencialesController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== LOGIN (delegado a AuthService) ====================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String usuario = credentials.get("usuario");
        String contrasena = credentials.get("contrasena");

        if (usuario == null || usuario.trim().isEmpty()
                || contrasena == null || contrasena.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Usuario y contraseña son obligatorios"));
        }

        // AuthService maneja: hasheo, bloqueo, intentos, excepciones
        Map<String, Object> respuesta = authService.login(usuario.trim(), contrasena);
        return ResponseEntity.ok(respuesta);
    }

    // ==================== REGISTRO PÚBLICO (siempre CLIENTE) ====================
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> datos) {
        String dniStr = datos.get("dni");
        String nombre = datos.get("nombre");
        String apellido = datos.get("apellido");
        String telefono = datos.getOrDefault("telefono", "");
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");
        String fechaNacimiento = datos.get("fechaNacimiento");

        if (dniStr == null || dniStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El DNI es obligatorio"));
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo/usuario es obligatorio"));
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La contraseña es obligatoria"));
        }

        // 1. Construir PersonaModel
        PersonaModel persona = new PersonaModel();
        persona.setDni(dniStr.trim());
        persona.setNombre(nombre != null ? nombre.trim() : "");
        persona.setApellido(apellido != null ? apellido.trim() : "");
        persona.setTelefono(telefono != null ? telefono.trim() : "");
        persona.setCorreo(usuario.trim());

        if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
            persona.setFechaNacimiento(fechaNacimiento);
        }

        // 2. Delegar toda la lógica a AuthService
        Map<String, Object> resultado = authService.registrarUsuario(
            persona,
            usuario.trim().toLowerCase(),
            contrasena,
            "CLIENTE"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // ==================== CREAR USUARIO POR ADMIN ====================
    @PostMapping("/admin/crear-usuario")
    public ResponseEntity<Map<String, Object>> crearUsuarioAdmin(@RequestBody Map<String, String> datos) {
        String dniStr = datos.get("dni");
        String nombre = datos.get("nombre");
        String apellido = datos.get("apellido");
        String telefono = datos.getOrDefault("telefono", "");
        String usuario = datos.get("usuario");
        String contrasena = datos.get("contrasena");
        String rol = datos.get("rol");

        if (rol == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol es obligatorio"));
        }
        if (dniStr == null || dniStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "DNI requerido"));
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario requerido"));
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contraseña requerida"));
        }

        PersonaModel persona = new PersonaModel();
        persona.setDni(dniStr.trim());
        persona.setNombre(nombre != null ? nombre.trim() : "");
        persona.setApellido(apellido != null ? apellido.trim() : "");
        persona.setTelefono(telefono != null ? telefono.trim() : "");
        persona.setCorreo(usuario.trim());

        // AuthService normaliza el rol y valida duplicados
        Map<String, Object> resultado = authService.registrarUsuario(
            persona,
            usuario.trim().toLowerCase(),
            contrasena,
            rol
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // ==================== OLVIDO CONTRASEÑA ====================
    @PostMapping("/olvide-contrasena")
    public ResponseEntity<Map<String, Object>> olvideContrasena(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");

        if (correo == null || correo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo es obligatorio"));
        }

        // Por seguridad, nunca revelar si el correo existe o no
        return ResponseEntity.ok(Map.of(
            "mensaje", "Si el correo existe, recibirás instrucciones para restablecer tu contraseña"
        ));
    }
}