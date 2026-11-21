package com.reparaciones.paginaweb.controllers;

<<<<<<< HEAD
import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
=======
import com.reparaciones.paginaweb.models.*;
import com.reparaciones.paginaweb.services.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder; // ← IMPORTANTE
>>>>>>> rama-secundaria
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class CredencialesController {

<<<<<<< HEAD
    private final AuthService authService;

    public CredencialesController(AuthService authService) {
        this.authService = authService;
=======
    private final CredencialesService credencialesService;
    private final PersonaService personaService;
    private final ClienteService clienteService;
    private final TecnicoService tecnicoService;
    private final AdministradorService administradorService;
    private final PasswordEncoder passwordEncoder; // ← NUEVO

    public CredencialesController(CredencialesService credencialesService,
                                   PersonaService personaService,
                                   ClienteService clienteService,
                                   TecnicoService tecnicoService,
                                   AdministradorService administradorService,
                                   PasswordEncoder passwordEncoder) { // ← NUEVO
        this.credencialesService = credencialesService;
        this.personaService = personaService;
        this.clienteService = clienteService;
        this.tecnicoService = tecnicoService;
        this.administradorService = administradorService;
        this.passwordEncoder = passwordEncoder; // ← NUEVO
>>>>>>> rama-secundaria
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

<<<<<<< HEAD
        if (dniStr == null || dniStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El DNI es obligatorio"));
=======
            System.out.println(">>> [REGISTRO PÚBLICO] Creando CLIENTE: " + usuario);

            // Validaciones
            if (dniStr == null || dniStr.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El DNI es obligatorio"));
            }
            
            if (usuario == null || usuario.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "El correo/usuario es obligatorio"));
            }

            if (contrasena == null || contrasena.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "La contraseña es obligatoria"));
            }

            if (credencialesService.existsByUsuario(usuario.trim())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El correo ya está registrado"));
            }

            if (personaService.existsByDni(dniStr.trim())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El DNI ya está registrado"));
            }

            // 1. Guardar Persona
            PersonaModel persona = new PersonaModel();
            persona.setDni(dniStr.trim());
            persona.setNombre(nombre != null ? nombre.trim() : "");
            persona.setApellido(apellido != null ? apellido.trim() : "");
            persona.setTelefono(telefono != null ? telefono.trim() : "");
            persona.setCorreo(usuario.trim());
            
            PersonaModel personaGuardada = personaService.save(persona);
            System.out.println(">>> [REGISTRO] Persona ID: " + personaGuardada.getIdPersona());

            // 2. Guardar Credenciales
            CredencialesModel cred = new CredencialesModel();
            cred.setIdPersona(personaGuardada.getIdPersona());
            cred.setUsuario(usuario.trim());
            // 🔐 AQUÍ APLICAMOS EL HASH:
            cred.setContrasena(passwordEncoder.encode(contrasena)); 
            cred.setRol(rol);
            cred.setEstado(1);  // 1 = ACTIVO
            
            credencialesService.save(cred);
            System.out.println(">>> [REGISTRO] Credencial guardada (con hash): " + usuario);

            // 3. Crear Cliente (SIEMPRE)
            ClienteModel cliente = new ClienteModel();
            cliente.setIdPersona(personaGuardada.getIdPersona());
            clienteService.save(cliente);
            System.out.println(">>> [REGISTRO] Cliente creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Cliente registrado exitosamente",
                        "idPersona", personaGuardada.getIdPersona(),
                        "usuario", usuario,
                        "rol", rol
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar: " + e.getMessage()));
>>>>>>> rama-secundaria
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

<<<<<<< HEAD
        if (rol == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El rol es obligatorio"));
=======
            System.out.println(">>> [ADMIN] Creando " + rol + ": " + usuario);

            // Validar rol permitido
            if (!rol.equals("CLIENTE") && !rol.equals("TECNICO") && 
                !rol.equals("TÉCNICO") && !rol.equals("ADMINISTRADOR") && 
                !rol.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Rol no válido. Use: CLIENTE, TECNICO, ADMINISTRADOR"));
            }

            
            if (rol.equals("TÉCNICO")) rol = "TECNICO";
            if (rol.equals("ADMIN")) rol = "ADMINISTRADOR";

            
            if (dniStr == null || dniStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "DNI requerido"));
            }
            if (usuario == null || usuario.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario requerido"));
            }
            if (credencialesService.existsByUsuario(usuario.trim())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El correo ya está registrado"));
            }

            // 1. Guardar Persona
            PersonaModel persona = new PersonaModel();
            persona.setDni(dniStr.trim());
            persona.setNombre(nombre.trim());
            persona.setApellido(apellido.trim());
            persona.setTelefono(telefono.trim());
            persona.setCorreo(usuario.trim());
            PersonaModel personaGuardada = personaService.save(persona);

            // 2. Guardar Credenciales
            CredencialesModel cred = new CredencialesModel();
            cred.setIdPersona(personaGuardada.getIdPersona());
            cred.setUsuario(usuario.trim());
            // 🔐 AQUÍ APLICAMOS EL HASH:
            cred.setContrasena(passwordEncoder.encode(contrasena)); 
            cred.setRol(rol);
            cred.setEstado(1);
            credencialesService.save(cred);

            // 3. Crear según rol
            switch (rol) {
                case "CLIENTE":
                    ClienteModel cliente = new ClienteModel();
                    cliente.setIdPersona(personaGuardada.getIdPersona());
                    clienteService.save(cliente);
                    break;
                    
                case "TECNICO":
                    TecnicoModel tecnico = new TecnicoModel();
                    tecnico.setIdPersona(personaGuardada.getIdPersona());
                    tecnicoService.save(tecnico);
                    break;
                    
                case "ADMINISTRADOR":
                    AdministradorModel admin = new AdministradorModel();
                    admin.setIdPersona(personaGuardada.getIdPersona());
                    administradorService.save(admin);
                    break;
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", rol + " creado exitosamente",
                        "idPersona", personaGuardada.getIdPersona(),
                        "usuario", usuario,
                        "rol", rol
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
>>>>>>> rama-secundaria
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