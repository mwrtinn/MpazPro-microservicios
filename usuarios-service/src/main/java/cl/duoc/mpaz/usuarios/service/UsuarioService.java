package cl.duoc.mpaz.usuarios.service;

import cl.duoc.mpaz.usuarios.dto.LoginRequest;
import cl.duoc.mpaz.usuarios.dto.UsuarioRequest;
import cl.duoc.mpaz.usuarios.dto.UsuarioResponse;
import cl.duoc.mpaz.usuarios.dto.UsuarioUpdateRequest;
import cl.duoc.mpaz.usuarios.model.Usuario;
import cl.duoc.mpaz.usuarios.repository.UsuarioRepository;
import cl.duoc.mpaz.usuarios.util.RutUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Reglas de negocio de usuarios: acceso por RUT y CRUD. */
@Service
public class UsuarioService {

    private static final String CURSO_POR_DEFECTO = "3° Básico";

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public UsuarioResponse login(LoginRequest request) {
        String rut = RutUtil.normalizar(request.rut());
        return repository.findById(rut)
                .filter(u -> u.getRol().equals(request.rol()))
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "RUT, contraseña o perfil incorrectos"));
    }

    public List<UsuarioResponse> listar(String rol) {
        List<Usuario> usuarios = (rol == null || rol.isBlank()) ? repository.findAll() : repository.findByRolOrderByNombre(rol);
        return usuarios.stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse obtener(String rut) {
        return UsuarioResponse.desde(buscar(RutUtil.normalizar(rut)));
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        String rut = RutUtil.normalizar(request.rut());
        if (!RutUtil.esValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUT no es válido");
        }
        if (repository.existsById(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        Usuario usuario = new Usuario(
                rut,
                request.nombre().trim(),
                encoder.encode(request.password()),
                request.rol(),
                conCursoPorDefecto(request.curso())
        );
        return UsuarioResponse.desde(repository.save(usuario));
    }

    public UsuarioResponse actualizar(String rut, UsuarioUpdateRequest request) {
        Usuario usuario = buscar(RutUtil.normalizar(rut));
        usuario.setNombre(request.nombre().trim());
        usuario.setRol(request.rol());
        usuario.setCurso(conCursoPorDefecto(request.curso()));
        // Si no viene contraseña, se conserva la actual
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(encoder.encode(request.password()));
        }
        return UsuarioResponse.desde(repository.save(usuario));
    }

    public void eliminar(String rut) {
        Usuario usuario = buscar(RutUtil.normalizar(rut));
        repository.delete(usuario);
    }

    private Usuario buscar(String rutNormalizado) {
        return repository.findById(rutNormalizado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ese RUT"));
    }

    private String conCursoPorDefecto(String curso) {
        return (curso == null || curso.isBlank()) ? CURSO_POR_DEFECTO : curso.trim();
    }
}