package cl.duoc.mpaz.aprendizaje.dto;

import cl.duoc.mpaz.aprendizaje.model.Pregunta;
import com.fasterxml.jackson.annotation.JsonInclude;

// Datos de una pregunta según el perfil del usuario.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreguntaResponse(
        Integer id,
        int oaId,
        String tipo,
        int dificultad,
        String enunciado,
        String opcionA,
        String opcionB,
        String opcionC,
        Integer correcta
) {

    // Oculta la alternativa correcta cuando consulta un estudiante.
    public static PreguntaResponse desde(
            Pregunta pregunta, boolean estudiante) {

        return new PreguntaResponse(
                pregunta.id,
                pregunta.oaId,
                pregunta.tipo,
                pregunta.dificultad,
                pregunta.enunciado,
                pregunta.opcionA,
                pregunta.opcionB,
                pregunta.opcionC,
                estudiante ? null : pregunta.correcta
        );
    }
}