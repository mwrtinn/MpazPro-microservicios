package cl.duoc.mpaz.aprendizaje.model;

import jakarta.persistence.*;

// Material educativo asociado a un objetivo de aprendizaje.
@Entity
@Table(name = "contenidos")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public int oaId;
    public String titulo;

    @Column(length = 5000)
    public String explicacion;

    @Column(length = 2000)
    public String ejemplo;

    public String icono;

    // Estado de revisión del contenido por parte de UTP.
    public String estado = "PENDIENTE";

    @Column(length = 2000)
    public String observacionUtp = "";

    @Column(length = 1000)
    public String imagenPath;
}