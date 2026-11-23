package cl.duoc.mpaz.usuarios.repository;

import cl.duoc.mpaz.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla usuarios (Spring Data JPA genera la implementación). */
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<Usuario> findByRolOrderByNombre(String rol);
}