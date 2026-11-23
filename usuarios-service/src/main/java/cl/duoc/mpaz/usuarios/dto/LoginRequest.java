package cl.duoc.mpaz.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/login. */
public record LoginRequest(
        @NotBlank(message = "El RUT es obligatorio") String rut,
        @NotBlank(message = "La contraseña es obligatoria") String password,
        @NotBlank(message = "El perfil es obligatorio") String rol
) {
}