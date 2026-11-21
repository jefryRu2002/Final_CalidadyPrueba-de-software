package com.reparaciones.paginaweb.services;

import com.reparaciones.paginaweb.models.CredencialesModel;
import com.reparaciones.paginaweb.repositories.CredencialesRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // ← IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CredencialesService {

    private final CredencialesRepository credencialesRepository;
    private final PasswordEncoder passwordEncoder; // ← NUEVO

    // Añadimos el PasswordEncoder al constructor
    public CredencialesService(CredencialesRepository credencialesRepository,
                               PasswordEncoder passwordEncoder) {
        this.credencialesRepository = credencialesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Obtener todas las credenciales
    public List<CredencialesModel> findAll() {
        return credencialesRepository.findAll();
    }

    // Buscar por ID (idPersona)
    public CredencialesModel findById(Integer id) {
        return credencialesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credenciales no encontradas: " + id));
    }

    // Buscar por usuario
    public Optional<CredencialesModel> findByUsuario(String usuario) {
        return credencialesRepository.findByUsuario(usuario);
    }

    // ==================== LOGIN CON HASHING ====================
    public Optional<CredencialesModel> login(String usuario, String contrasenaPlana) {
        // 1. Buscamos SOLO por el nombre de usuario (ya no por contraseña)
        Optional<CredencialesModel> credencialOpt = credencialesRepository.findByUsuario(usuario);

        // 2. Si el usuario existe, comprobamos la contraseña
        if (credencialOpt.isPresent()) {
            CredencialesModel cred = credencialOpt.get();
            String hashEnBaseDeDatos = cred.getContrasena();
            
            // 3. Comparamos el texto plano con el hash de la BD
            if (passwordEncoder.matches(contrasenaPlana, hashEnBaseDeDatos)) {
                return Optional.of(cred); // Login exitoso
            }
        }

        // Si no existe el usuario o la contraseña no coincide
        return Optional.empty();
    }

    // Verificar si existe un usuario
    public boolean existsByUsuario(String usuario) {
        return credencialesRepository.existsByUsuario(usuario);
    }

    // Guardar nueva credencial
    public CredencialesModel save(CredencialesModel credenciales) {
        if (credencialesRepository.existsByUsuario(credenciales.getUsuario())) {
            throw new RuntimeException("El usuario ya existe");
        }
        return credencialesRepository.save(credenciales);
    }

    // Actualizar credencial
    public CredencialesModel update(Integer id, CredencialesModel credenciales) {
        CredencialesModel existente = findById(id);
        
        if (credenciales.getUsuario() != null) {
            existente.setUsuario(credenciales.getUsuario());
        }
        
        if (credenciales.getContrasena() != null) {
            // 🔐 HASHEAMOS LA NUEVA CONTRASEÑA ANTES DE GUARDARLA
            existente.setContrasena(passwordEncoder.encode(credenciales.getContrasena()));
        }
        
        if (credenciales.getRol() != null) {
            existente.setRol(credenciales.getRol());
        }
        if (credenciales.getEstado() != null) {
            existente.setEstado(credenciales.getEstado());
        }
        
        return credencialesRepository.save(existente);
    }

    // Eliminar credencial
    public void delete(Integer id) {
        findById(id); // Verifica que existe
        credencialesRepository.deleteById(id);
    }
}