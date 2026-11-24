package cl.duoc.mpaz.aprendizaje.repository;

import cl.duoc.mpaz.aprendizaje.model.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Consultas de respuestas, progreso e intentos.
public interface RespuestaRepository
        extends JpaRepository<Respuesta, Integer> {

    List<Respuesta> findByRutAlumno(String rutAlumno);

    List<Respuesta> findByRutAlumnoAndOaId(
            String rutAlumno, int oaId);

    boolean existsByRutAlumnoAndPreguntaIdAndIntento(
            String rutAlumno, int preguntaId, int intento);

    // Comprueba si existen resultados asociados.
    boolean existsByOaId(int oaId);

    boolean existsByPreguntaId(int preguntaId);

    // Recupera el último intento registrado.
    @Query("""
            SELECT COALESCE(MAX(r.intento), 0)
            FROM Respuesta r
            WHERE r.rutAlumno = :rut
            AND r.oaId = :oaId
            AND r.tipo = :tipo
            """)
    int ultimoIntento(
            @Param("rut") String rut,
            @Param("oaId") int oaId,
            @Param("tipo") String tipo);
}