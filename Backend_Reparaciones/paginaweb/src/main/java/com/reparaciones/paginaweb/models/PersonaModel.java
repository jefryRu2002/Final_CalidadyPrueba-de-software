package com.reparaciones.paginaweb.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "persona")
public class PersonaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Integer idPersona;

<<<<<<< HEAD
    @Column(name = "nombre", length = 100)
    private String nombre;  // En BD es "nombres"

    @Column(name = "apellido", length = 100)
    private String apellido;  // En BD es "apellidos"
=======
    @Column(name = "nombres", length = 100)
    private String nombre;

    @Column(name = "apellidos", length = 100)
    private String apellido;
>>>>>>> rama-secundaria

    @Column(length = 255)
    private String correo;

    @Column(length = 8)
    private String dni;

    @Column(length = 9)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private String fechaNacimiento;

    @Column(length = 255)
    private String estado;

    // ===== NUEVOS campos de bloqueo =====
    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "bloqueado_permanente", nullable = false)
    private Boolean bloqueadoPermanente = false;

    // ===== Getters / Setters existentes =====
    public PersonaModel() {}

    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // ===== NUEVOS getters / setters =====
    public Integer getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(Integer intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public Boolean getBloqueadoPermanente() { return bloqueadoPermanente; }
    public void setBloqueadoPermanente(Boolean bloqueadoPermanente) { this.bloqueadoPermanente = bloqueadoPermanente; }
}