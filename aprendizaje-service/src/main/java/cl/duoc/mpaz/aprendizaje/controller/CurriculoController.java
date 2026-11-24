package cl.duoc.mpaz.aprendizaje.controller;

import cl.duoc.mpaz.aprendizaje.dto.PreguntaResponse;
import cl.duoc.mpaz.aprendizaje.model.Contenido;
import cl.duoc.mpaz.aprendizaje.model.Objetivo;
import cl.duoc.mpaz.aprendizaje.model.Pregunta;
import cl.duoc.mpaz.aprendizaje.service.CurriculoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints REST de objetivos, contenidos y preguntas.
@RestController
@RequestMapping("/api")
public class CurriculoController {

    private final CurriculoService service;

    public CurriculoController(CurriculoService service) {
        this.service = service;
    }

    // Identifica el perfil desde el token autenticado.
    private boolean esEstudiante(Jwt jwt) {
        return "ESTUDIANTE".equals(
                jwt.getClaimAsString("rol"));
    }

    // ---------- Objetivos ----------

    @GetMapping("/objetivos")
    public List<Objetivo> listarObjetivos(
            @AuthenticationPrincipal Jwt jwt) {

        return service.listarObjetivos(esEstudiante(jwt));
    }

    @GetMapping("/objetivos/{id}")
    public Objetivo buscarObjetivo(
            @PathVariable int id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.consultarObjetivo(
                id, esEstudiante(jwt));
    }

    @PostMapping("/objetivos")
    @ResponseStatus(HttpStatus.CREATED)
    public Objetivo crearObjetivo(
            @RequestBody Objetivo objetivo) {

        return service.crearObjetivo(objetivo);
    }

    @PutMapping("/objetivos/{id}")
    public Objetivo actualizarObjetivo(
            @PathVariable int id,
            @RequestBody Objetivo objetivo) {

        return service.actualizarObjetivo(id, objetivo);
    }

    @DeleteMapping("/objetivos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarObjetivo(@PathVariable int id) {
        service.eliminarObjetivo(id);
    }

    // ---------- Contenidos ----------

    @GetMapping("/contenidos")
    public List<Contenido> listarContenidos(
            @RequestParam(required = false) Integer oaId,
            @AuthenticationPrincipal Jwt jwt) {

        return service.listarContenidos(
                oaId, esEstudiante(jwt));
    }

    @GetMapping("/contenidos/{id}")
    public Contenido buscarContenido(
            @PathVariable int id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.consultarContenido(
                id, esEstudiante(jwt));
    }

    @PostMapping("/contenidos")
    @ResponseStatus(HttpStatus.CREATED)
    public Contenido crearContenido(
            @RequestBody Contenido contenido) {

        return service.crearContenido(contenido);
    }

    @PutMapping("/contenidos/{id}")
    public Contenido actualizarContenido(
            @PathVariable int id,
            @RequestBody Contenido contenido) {

        return service.actualizarContenido(id, contenido);
    }

    // Endpoint reservado para la revisión de UTP.
    @PatchMapping("/contenidos/{id}/estado")
    public Contenido revisarContenido(
            @PathVariable int id,
            @RequestBody RevisionRequest revision) {

        return service.revisarContenido(
                id,
                revision.estado(),
                revision.observacionUtp());
    }

    @DeleteMapping("/contenidos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarContenido(@PathVariable int id) {
        service.eliminarContenido(id);
    }

    // ---------- Preguntas ----------

    @GetMapping("/preguntas")
    public List<PreguntaResponse> listarPreguntas(
            @RequestParam(required = false) Integer oaId,
            @RequestParam(required = false) String tipo,
            @AuthenticationPrincipal Jwt jwt) {

        return service.listarPreguntas(
                oaId, tipo, esEstudiante(jwt));
    }

    @GetMapping("/preguntas/{id}")
    public PreguntaResponse buscarPregunta(
            @PathVariable int id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.consultarPregunta(
                id, esEstudiante(jwt));
    }

    @PostMapping("/preguntas")
    @ResponseStatus(HttpStatus.CREATED)
    public Pregunta crearPregunta(
            @RequestBody Pregunta pregunta) {

        return service.crearPregunta(pregunta);
    }

    @PutMapping("/preguntas/{id}")
    public Pregunta actualizarPregunta(
            @PathVariable int id,
            @RequestBody Pregunta pregunta) {

        return service.actualizarPregunta(id, pregunta);
    }

    @DeleteMapping("/preguntas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPregunta(@PathVariable int id) {
        service.eliminarPregunta(id);
    }

    // Datos enviados por UTP durante la revisión.
    public record RevisionRequest(
            String estado,
            String observacionUtp
    ) {
    }
}