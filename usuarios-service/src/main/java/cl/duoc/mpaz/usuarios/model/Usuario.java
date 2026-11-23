package cl.duoc.mpaz.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Usuario ficticio de la plataforma. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 9)
    private String rut;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false, length = 40)
    private String curso;

    public Usuario() {
    }

    public Usuario(String rut, String nombre, String passwordHash, String rol, String curso) {
        this.rut = rut;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.curso = curso;
    }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
}