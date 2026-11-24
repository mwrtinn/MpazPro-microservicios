package cl.duoc.mpaz.aprendizaje.repository;

import cl.duoc.mpaz.aprendizaje.model.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Consultas de contenidos y estados de aprobación.
public interface ContenidoRepository
        extends JpaRepository<Contenido, Integer> {

    List<Contenido> findByOaId(int oaId);

    List<Contenido> findByOaIdAndEstado(
            int oaId, String estado);

    List<Contenido> findByEstado(String estado);
}