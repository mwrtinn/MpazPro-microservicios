package cl.duoc.mpaz.aprendizaje.model;

import jakarta.persistence.*;

// Representa un objetivo de aprendizaje del Currículum Nacional.
@Entity
@Table(name = "objetivos")
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public String asignatura;
    public String unidad;
    public String codigo;
    public String eje;

    @Column(length = 2000)
    public String descripcion;

    @Column(length = 1000)
    public String referencia;

    // Configuración realizada por el docente.
    public boolean asignado = false;
    public int intentosPrueba = 2;
}