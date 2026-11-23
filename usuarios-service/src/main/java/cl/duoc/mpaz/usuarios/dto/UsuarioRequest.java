package cl.duoc.mpaz.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/usuarios (crear un usuario). */
public record UsuarioRequest(
        @NotBlank(message = "El RUT es obligatorio") String rut,
        @NotBlank @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres") String nombre,
        @NotBlank @Size(min = 4, max = 64, message = "La contraseña debe tener entre 4 y 64 caracteres") String password,
        @NotBlank @Pattern(regexp = "ESTUDIANTE|DOCENTE|UTP", message = "El perfil debe ser ESTUDIANTE, DOCENTE o UTP") String rol,
        String curso
) {
}