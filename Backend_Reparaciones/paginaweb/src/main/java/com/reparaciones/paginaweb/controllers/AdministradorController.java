package com.reparaciones.paginaweb.controllers;

import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.services.AuthService;
import com.reparaciones.paginaweb.services.NotificacionService;
import com.reparaciones.paginaweb.services.PersonaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdministradorController {

    private final AuthService authService;
    private final NotificacionService notificacionService;
    private final PersonaService personaService;   // ← NUEVO

    public AdministradorController(AuthService authService,
                                    NotificacionService notificacionService,
                                    PersonaService personaService) {   // ← NUEVO
        this.authService = authService;
        this.notificacionService = notificacionService;
        this.personaService = personaService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> getAllUsuarios() {
        try {
            List<Map<String, Object>> usuarios = authService.obtenerTodosUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                            @RequestBody Map<String, Integer> body) {
        try {
            Map<String, Object> resultado = authService.cambiarEstado(id, body.get("estado"));

            // 🔔 NOTIFICACIÓN: Cambio de estado de usuario
            Integer nuevoEstado = body.get("estado");
            String estadoStr = (nuevoEstado != null && nuevoEstado == 1) ? "Activado" : "Desactivado";
            notificacionService.crearNotificacion(
                "USUARIO",
                "Usuario " + estadoStr,
                "El usuario #" + id + " ha sido " + estadoStr.toLowerCase() + " por el administrador.",
                id
            );

            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Integer id,
                                         @RequestBody Map<String, String> body) {
        try {
            Map<String, Object> resultado = authService.cambiarRol(id, body.get("rol"));

            // 🔔 NOTIFICACIÓN: Cambio de rol
            notificacionService.crearNotificacion(
                "USUARIO",
                "Rol de Usuario Cambiado",
                "El usuario #" + id + " ahora tiene el rol: " + body.get("rol"),
                id
            );

            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== NUEVO: BLOQUEO / DESBLOQUEO ====================

    /** GET /api/admin/usuarios-bloqueados */
    @GetMapping("/usuarios-bloqueados")
    public ResponseEntity<?> getUsuariosBloqueados() {
        try {
            List<PersonaModel> bloqueados = personaService.findAll().stream()
                    .filter(personaService::estaBloqueada)
                    .toList();
            return ResponseEntity.ok(bloqueados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    /** PUT /api/admin/usuarios/{id}/desbloquear */
    @PutMapping("/usuarios/{id}/desbloquear")
    public ResponseEntity<?> desbloquearUsuario(@PathVariable Integer id) {
        try {
            PersonaModel persona = personaService.desbloquear(id);

            // 🔔 NOTIFICACIÓN: Usuario desbloqueado
            notificacionService.crearNotificacion(
                "USUARIO",
                "Usuario Desbloqueado",
                "El usuario #" + id + " ha sido desbloqueado por el administrador.",
                id
            );

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario desbloqueado correctamente",
                    "idPersona", persona.getIdPersona()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/admin/usuarios/{id}/bloquear */
    @PutMapping("/usuarios/{id}/bloquear")
    public ResponseEntity<?> bloquearUsuario(@PathVariable Integer id) {
        try {
            PersonaModel persona = personaService.bloquearPermanente(id);

            // 🔔 NOTIFICACIÓN: Usuario bloqueado
            notificacionService.crearNotificacion(
                "USUARIO",
                "Usuario Bloqueado",
                "El usuario #" + id + " ha sido bloqueado permanentemente por el administrador.",
                id
            );

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario bloqueado permanentemente",
                    "idPersona", persona.getIdPersona()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}