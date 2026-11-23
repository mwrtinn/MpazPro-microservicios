package cl.duoc.mpaz.usuarios.controller;

import cl.duoc.mpaz.usuarios.dto.UsuarioRequest;
import cl.duoc.mpaz.usuarios.dto.UsuarioResponse;
import cl.duoc.mpaz.usuarios.dto.UsuarioUpdateRequest;
import cl.duoc.mpaz.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD de usuarios: /api/usuarios. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) String rol) {
        return service.listar(rol);
    }

    @GetMapping("/{rut}")
    public UsuarioResponse obtener(@PathVariable String rut) {
        return service.obtener(rut);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{rut}")
    public UsuarioResponse actualizar(@PathVariable String rut, @Valid @RequestBody UsuarioUpdateRequest request) {
        return service.actualizar(rut, request);
    }

    @DeleteMapping("/{rut}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String rut) {
        service.eliminar(rut);
    }
}