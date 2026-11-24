package cl.duoc.mpaz.aprendizaje.repository;

import cl.duoc.mpaz.aprendizaje.model.Objetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acceso a los objetivos de aprendizaje.
public interface ObjetivoRepository
        extends JpaRepository<Objetivo, Integer> {

    List<Objetivo> findByAsignadoTrue();
}