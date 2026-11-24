package cl.duoc.mpaz.aprendizaje.repository;

import cl.duoc.mpaz.aprendizaje.model.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Consultas de preguntas por objetivo, tipo y dificultad.
public interface PreguntaRepository
        extends JpaRepository<Pregunta, Integer> {

    List<Pregunta> findByOaId(int oaId);

    List<Pregunta> findByOaIdAndTipoOrderByDificultadAscIdAsc(
            int oaId, String tipo);
}