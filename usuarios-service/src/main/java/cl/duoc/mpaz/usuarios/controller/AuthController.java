package cl.duoc.mpaz.usuarios.controller;

import cl.duoc.mpaz.usuarios.dto.LoginRequest;
import cl.duoc.mpaz.usuarios.dto.LoginResponse;
import cl.duoc.mpaz.usuarios.dto.UsuarioResponse;
import cl.duoc.mpaz.usuarios.security.JwtService;
import cl.duoc.mpaz.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controlador encargado de la autenticación de usuarios.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService service;
    private final JwtService jwtService;

    public AuthController(
            UsuarioService service,
            JwtService jwtService) {

        this.service = service;
        this.jwtService = jwtService;
    }

    // Valida las credenciales y genera el token de acceso.
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        UsuarioResponse usuario = service.login(request);

        String token = jwtService.generarToken(usuario);

        return LoginResponse.desde(usuario, token);
    }
}