package cl.duoc.mpaz.aprendizaje.repository;

import cl.duoc.mpaz.aprendizaje.model.Informe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acceso a los informes pedagógicos.
public interface InformeRepository
        extends JpaRepository<Informe, String> {

    List<Informe> findByRutAlumno(String rutAlumno);

    List<Informe> findByOaId(int oaId);
}