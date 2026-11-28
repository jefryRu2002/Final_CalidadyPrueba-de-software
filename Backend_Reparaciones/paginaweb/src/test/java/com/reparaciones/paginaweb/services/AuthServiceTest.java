package com.reparaciones.paginaweb.services;

import com.reparaciones.paginaweb.models.ClienteModel;
import com.reparaciones.paginaweb.models.CredencialesModel;
import com.reparaciones.paginaweb.models.PersonaModel;
import com.reparaciones.paginaweb.repositories.AdministradorRepository;
import com.reparaciones.paginaweb.repositories.ClienteRepository;
import com.reparaciones.paginaweb.repositories.CredencialesRepository;
import com.reparaciones.paginaweb.repositories.PersonaRepository;
import com.reparaciones.paginaweb.repositories.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private CredencialesRepository credencialesRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private TecnicoRepository tecnicoRepository;
    @Mock
    private AdministradorRepository administradorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsUserAndPersonaDataWhenCredentialsAreValid() {
        CredencialesModel credenciales = new CredencialesModel();
        credenciales.setIdPersona(10);
        credenciales.setUsuario("cliente@example.com");
        credenciales.setContrasena("hashed-password");
        credenciales.setRol("CLIENTE");
        credenciales.setEstado(1);

        PersonaModel persona = new PersonaModel();
        persona.setNombre("Ana");
        persona.setApellido("Pérez");
        persona.setCorreo("cliente@example.com");
        persona.setDni("12345678");
        persona.setTelefono("999999999");

        when(credencialesRepository.findByUsuario("cliente@example.com"))
                .thenReturn(Optional.of(credenciales));
        when(passwordEncoder.matches("clave", "hashed-password")).thenReturn(true);
        when(personaRepository.findById(10)).thenReturn(Optional.of(persona));

        Map<String, Object> response = authService.login("cliente@example.com", "clave");

        assertEquals(10, response.get("idPersona"));
        assertEquals("CLIENTE", response.get("rol"));
        assertEquals("Ana", response.get("nombre"));
        assertEquals("Pérez", response.get("apellido"));
        assertEquals("12345678", response.get("dni"));
    }

    @Test
    void loginRejectsUnknownUser() {
        when(credencialesRepository.findByUsuario("missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> authService.login("missing", "clave"));

        assertEquals("USUARIO_NO_ENCONTRADO", exception.getMessage());
    }

    @Test
    void loginRejectsInactiveUserBeforeCheckingPassword() {
        CredencialesModel credenciales = new CredencialesModel();
        credenciales.setEstado(0);
        when(credencialesRepository.findByUsuario("inactivo"))
                .thenReturn(Optional.of(credenciales));

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> authService.login("inactivo", "clave"));

        assertEquals("Usuario desactivado. Contacte al administrador.", exception.getMessage());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        CredencialesModel credenciales = new CredencialesModel();
        credenciales.setEstado(1);
        credenciales.setContrasena("hashed-password");
        when(credencialesRepository.findByUsuario("cliente"))
                .thenReturn(Optional.of(credenciales));
        when(passwordEncoder.matches("incorrecta", "hashed-password")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> authService.login("cliente", "incorrecta"));

        assertEquals("CONTRASENA_INCORRECTA", exception.getMessage());
    }

    @Test
    void registrarUsuarioStoresEncodedCredentialsAndCreatesClientRole() {
        PersonaModel persona = new PersonaModel();
        persona.setIdPersona(10);
        persona.setDni("12345678");
        persona.setNombre("Ana");
        persona.setApellido("Pérez");
        persona.setCorreo("ana@example.com");
        persona.setTelefono("999999999");

        when(personaRepository.existsByDni("12345678")).thenReturn(false);
        when(credencialesRepository.existsByUsuario("ana@example.com")).thenReturn(false);
        when(personaRepository.save(persona)).thenReturn(persona);
        when(passwordEncoder.encode("clave")).thenReturn("hashed-password");

        Map<String, Object> response =
                authService.registrarUsuario(persona, "ana@example.com", "clave", "cliente");

        assertEquals("CLIENTE", response.get("rol"));
        assertEquals(10, response.get("idPersona"));
        ArgumentCaptor<CredencialesModel> credentialsCaptor =
                ArgumentCaptor.forClass(CredencialesModel.class);
        verify(credencialesRepository).save(credentialsCaptor.capture());
        assertEquals("hashed-password", credentialsCaptor.getValue().getContrasena());
        assertEquals("CLIENTE", credentialsCaptor.getValue().getRol());
        verify(clienteRepository).save(any(ClienteModel.class));
    }
}
