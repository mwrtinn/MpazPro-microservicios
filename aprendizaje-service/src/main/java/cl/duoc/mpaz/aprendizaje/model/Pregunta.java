package cl.duoc.mpaz.aprendizaje.model;

import jakarta.persistence.*;

// Preguntas utilizadas en desafíos y evaluaciones finales.
@Entity
@Table(name = "preguntas")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public int oaId;

    // Tipo de pregunta: DESAFIO o PRUEBA.
    public String tipo;

    // Nivel de dificultad entre 1 y 3.
    public int dificultad;

    @Column(length = 2000)
    public String enunciado;

    @Column(length = 1000)
    public String opcionA;

    @Column(length = 1000)
    public String opcionB;

    @Column(length = 1000)
    public String opcionC;

    // Índice de la alternativa correcta: 0, 1 o 2.
    public int correcta;
}