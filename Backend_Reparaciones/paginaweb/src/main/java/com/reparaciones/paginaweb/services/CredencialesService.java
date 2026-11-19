package com.reparaciones.paginaweb.services;

import com.reparaciones.paginaweb.models.CredencialesModel;
import com.reparaciones.paginaweb.repositories.CredencialesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CredencialesService {

    private final CredencialesRepository credencialesRepository;

    public CredencialesService(CredencialesRepository credencialesRepository) {
        this.credencialesRepository = credencialesRepository;
    }

    public List<CredencialesModel> findAll() {
        return credencialesRepository.findAll();
    }

    public CredencialesModel findById(Integer id) {
        return credencialesRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Credenciales no encontradas: " + id));
    }

    public Optional<CredencialesModel> findByUsuario(String usuario) {
        return credencialesRepository.findByUsuario(usuario);
    }

    public boolean existsByUsuario(String usuario) {
        return credencialesRepository.existsByUsuario(usuario);
    }

    public CredencialesModel save(CredencialesModel credenciales) {
        if (credencialesRepository.existsByUsuario(credenciales.getUsuario())) {
            throw new RuntimeException("El usuario ya existe");
        }
        return credencialesRepository.save(credenciales);
    }

    public CredencialesModel update(Integer id, CredencialesModel credenciales) {
        CredencialesModel existente = findById(id);

        if (credenciales.getUsuario() != null) existente.setUsuario(credenciales.getUsuario());
        if (credenciales.getContrasena() != null) existente.setContrasena(credenciales.getContrasena());
        if (credenciales.getRol() != null) existente.setRol(credenciales.getRol());
        if (credenciales.getEstado() != null) existente.setEstado(credenciales.getEstado());

        return credencialesRepository.save(existente);
    }

    public void delete(Integer id) {
        findById(id);
        credencialesRepository.deleteById(id);
    }
}