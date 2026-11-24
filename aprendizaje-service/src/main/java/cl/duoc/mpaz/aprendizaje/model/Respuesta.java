package cl.duoc.mpaz.aprendizaje.model;

import jakarta.persistence.*;

// Registro de las respuestas y los intentos del estudiante.
@Entity
@Table(
        name = "respuestas",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "rut_alumno",
                        "pregunta_id",
                        "intento"
                }
        )
)
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "rut_alumno", nullable = false)
    public String rutAlumno;

    public int oaId;

    @Column(name = "pregunta_id", nullable = false)
    public int preguntaId;

    public String tipo;
    public int dificultad;

    // Número de intento y alternativa seleccionada.
    public int intento;
    public int elegida;

    // Resultado calculado por el servidor.
    public boolean esCorrecta;

    public long fecha;
}