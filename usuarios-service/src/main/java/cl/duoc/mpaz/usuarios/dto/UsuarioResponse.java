package cl.duoc.mpaz.usuarios.dto;

import cl.duoc.mpaz.usuarios.model.Usuario;

/** Lo que el servicio devuelve de un usuario. */
public record UsuarioResponse(String rut, String nombre, String rol, String curso) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getRut(), u.getNombre(), u.getRol(), u.getCurso());
    }
}