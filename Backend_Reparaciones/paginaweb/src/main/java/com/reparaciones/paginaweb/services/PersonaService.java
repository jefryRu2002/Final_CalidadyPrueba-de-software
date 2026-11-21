package com.reparaciones.paginaweb.services;

import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.repositories.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    // ==================== CRUD EXISTENTE ====================

    public List<PersonaModel> findAll() {
        return personaRepository.findAll();
    }

    public PersonaModel findById(Integer id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada: " + id));
    }

    public PersonaModel save(PersonaModel persona) {
        return personaRepository.save(persona);
    }

    public PersonaModel update(Integer id, PersonaModel persona) {
        findById(id);
        persona.setIdPersona(id);
        return personaRepository.save(persona);
    }

    public void delete(Integer id) {
        findById(id);
        personaRepository.deleteById(id);
    }

    public PersonaModel findByDni(String dni) {
        return personaRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con DNI: " + dni));
    }

    public boolean existsByDni(String dni) {
        return personaRepository.existsByDni(dni);
    }

    // ==================== NUEVO: BÚSQUEDA POR CORREO ====================

    public PersonaModel findByCorreo(String correo) {
        return personaRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con correo: " + correo));
    }

    // ==================== NUEVO: LÓGICA DE BLOQUEO ====================

    /**
     * ¿La persona está bloqueada AHORA?
     * - Bloqueo permanente (admin) → siempre true.
     * - Bloqueo temporal → true si bloqueadoHasta > ahora.
     */
    public boolean estaBloqueada(PersonaModel persona) {
        if (persona == null) return false;
        if (Boolean.TRUE.equals(persona.getBloqueadoPermanente())) return true;
        return persona.getBloqueadoHasta() != null
                && persona.getBloqueadoHasta().isAfter(LocalDateTime.now());
    }

    /**
     * Suma un intento fallido. Si llega a maxIntentos, activa el bloqueo temporal.
     * @return los intentos acumulados tras el fallo.
     */
    public int registrarIntentoFallido(PersonaModel persona, int maxIntentos, int minutosBloqueo) {
        int intentos = (persona.getIntentosFallidos() == null ? 0 : persona.getIntentosFallidos()) + 1;
        persona.setIntentosFallidos(intentos);

        if (intentos >= maxIntentos) {
            persona.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutosBloqueo));
        }
        personaRepository.save(persona);
        return intentos;
    }

    /** Resetea contadores (llamado en login exitoso). */
    public void resetearIntentos(PersonaModel persona) {
        persona.setIntentosFallidos(0);
        persona.setBloqueadoHasta(null);
        persona.setBloqueadoPermanente(false);
        personaRepository.save(persona);
    }

    /** SOLO ADMIN: desbloqueo manual. */
    public PersonaModel desbloquear(Integer idPersona) {
        PersonaModel persona = findById(idPersona);
        persona.setIntentosFallidos(0);
        persona.setBloqueadoHasta(null);
        persona.setBloqueadoPermanente(false);
        return personaRepository.save(persona);
    }

    /** SOLO ADMIN: bloqueo permanente manual. */
    public PersonaModel bloquearPermanente(Integer idPersona) {
        PersonaModel persona = findById(idPersona);
        persona.setBloqueadoPermanente(true);
        persona.setBloqueadoHasta(null);
        return personaRepository.save(persona);
    }
}