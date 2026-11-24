package cl.duoc.mpaz.usuarios.dto;

// Respuesta del inicio de sesión con los datos del usuario y su token JWT.
public record LoginResponse(
        String rut,
        String nombre,
        String rol,
        String curso,
        String token
) {

    // Construye la respuesta con los datos del usuario autenticado.
    public static LoginResponse desde(
            UsuarioResponse usuario, String token) {

        return new LoginResponse(
                usuario.rut(),
                usuario.nombre(),
                usuario.rol(),
                usuario.curso(),
                token
        );
    }
}