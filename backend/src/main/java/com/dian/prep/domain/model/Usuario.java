package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Usuario {

    private UUID id;
    private String nombre;
    private String email;
    private String password;
    private RolUsuario rol;
    private boolean activo;
    private LocalDateTime createdAt;

    public enum RolUsuario {
        ESTUDIANTE, ADMIN
    }

    // Constructor para crear nuevo usuario
    public static Usuario crear(String nombre, String email, String passwordEncoded) {
        Usuario u = new Usuario();
        u.nombre    = nombre;
        u.email     = email;
        u.password  = passwordEncoded;
        u.rol       = RolUsuario.ESTUDIANTE;
        u.activo    = true;
        u.createdAt = LocalDateTime.now();
        return u;
    }

    // Getters y setters
    public UUID getId()                        { return id; }
    public void setId(UUID id)                 { this.id = id; }
    public String getNombre()                  { return nombre; }
    public void setNombre(String nombre)       { this.nombre = nombre; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }
    public RolUsuario getRol()                 { return rol; }
    public void setRol(RolUsuario rol)         { this.rol = rol; }
    public boolean isActivo()                  { return activo; }
    public void setActivo(boolean activo)      { this.activo = activo; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
}
