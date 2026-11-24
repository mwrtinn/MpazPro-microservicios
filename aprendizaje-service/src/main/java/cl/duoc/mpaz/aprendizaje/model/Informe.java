package cl.duoc.mpaz.aprendizaje.model;

import jakarta.persistence.*;

// Informe pedagógico reservado para el perfil docente.
@Entity
@Table(name = "informes")
public class Informe {

    // Identificador compuesto por el RUT y el ID del objetivo.
    @Id
    public String clave;

    public String rutAlumno;
    public int oaId;
    public int rendimiento;

    @Column(length = 5000)
    public String fortalezas;

    @Column(length = 5000)
    public String refuerzo;

    public String fecha;

    // El docente debe revisar y validar el informe.
    public boolean validado = false;
}