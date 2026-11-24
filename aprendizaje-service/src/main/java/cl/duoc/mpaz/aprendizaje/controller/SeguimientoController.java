package cl.duoc.mpaz.aprendizaje.controller;

import cl.duoc.mpaz.aprendizaje.model.Informe;
import cl.duoc.mpaz.aprendizaje.model.Respuesta;
import cl.duoc.mpaz.aprendizaje.service.SeguimientoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

// Endpoints REST de respuestas, intentos e informes pedagógicos.
@RestController
@RequestMapping("/api")
public class SeguimientoController {

    private final SeguimientoService service;

    public SeguimientoController(SeguimientoService service) {
        this.service = service;
    }

    // Identifica el perfil mediante el token autenticado.
    private boolean esEstudiante(Jwt jwt) {

        return "ESTUDIANTE".equals(
                jwt.getClaimAsString("rol"));
    }

    // ---------- Respuestas ----------

    @GetMapping("/respuestas")
    public List<Respuesta> listarRespuestas(
            @AuthenticationPrincipal Jwt jwt) {

        return service.listarRespuestas(
                jwt.getSubject(),
                esEstudiante(jwt));
    }

    @GetMapping("/respuestas/alumno/{rut}")
    public List<Respuesta> respuestasDeAlumno(
            @PathVariable String rut,
            @AuthenticationPrincipal Jwt jwt) {

        return service.respuestasDeAlumno(
                rut,
                jwt.getSubject(),
                esEstudiante(jwt));
    }

    // Registra la respuesta utilizando el RUT del token.
    @PostMapping("/respuestas")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestaResultado registrarRespuesta(
            @Valid @RequestBody RespuestaRequest datos,
            @AuthenticationPrincipal Jwt jwt) {

        Respuesta respuesta = service.registrarRespuesta(
                jwt.getSubject(),
                datos.preguntaId(),
                datos.intento(),
                datos.elegida());

        return new RespuestaResultado(
                respuesta.id,
                respuesta.rutAlumno,
                respuesta.oaId,
                respuesta.preguntaId,
                respuesta.tipo,
                respuesta.dificultad,
                respuesta.intento,
                respuesta.elegida,
                respuesta.esCorrecta,
                respuesta.fecha,
                service.alternativaCorrecta(respuesta.preguntaId)
        );
    }

    // ---------- Intentos ----------

    @GetMapping("/intentos")
    public Map<String, Object> consultarIntentos(
            @RequestParam(required = false) String rut,
            @RequestParam int oaId,
            @RequestParam String tipo,
            @AuthenticationPrincipal Jwt jwt) {

        String rutAlumno = rut == null
                ? jwt.getSubject()
                : rut;

        if (esEstudiante(jwt)
                && !jwt.getSubject().equals(rutAlumno)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar intentos de otro estudiante");
        }

        return service.consultarIntentos(
                rutAlumno, oaId, tipo);
    }

    // ---------- Informes exclusivos del docente ----------

    @GetMapping("/informes")
    public List<Informe> listarInformes() {
        return service.listarInformes();
    }

    @GetMapping("/informes/{clave}")
    public Informe buscarInforme(
            @PathVariable String clave) {

        return service.buscarInforme(clave);
    }

    // Genera o reemplaza el informe del estudiante.
    @PutMapping("/informes/{clave}")
    public Informe generarInforme(
            @PathVariable String clave,
            @Valid @RequestBody InformeRequest datos) {

        String claveEsperada =
                datos.rutAlumno() + "-" + datos.oaId();

        if (!claveEsperada.equals(clave)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Clave de informe inválida");
        }

        return service.generarInforme(
                datos.rutAlumno(),
                datos.oaId());
    }

    @PatchMapping("/informes/{clave}/validar")
    public Informe validarInforme(
            @PathVariable String clave) {

        return service.validarInforme(clave);
    }

    // ---------- Datos de las solicitudes y respuestas ----------

    // El estudiante solo envía la pregunta, intento y alternativa.
    public record RespuestaRequest(
            @NotNull @Min(1) Integer preguntaId,
            @NotNull @Min(1) Integer intento,
            @NotNull @Min(0) @Max(2) Integer elegida
    ) {
    }

    // Devuelve el resultado después de registrar la respuesta.
    public record RespuestaResultado(
            Integer id,
            String rutAlumno,
            int oaId,
            int preguntaId,
            String tipo,
            int dificultad,
            int intento,
            int elegida,
            boolean esCorrecta,
            long fecha,
            int correcta
    ) {
    }

    // Datos necesarios para generar un informe.
    public record InformeRequest(
            @NotBlank String rutAlumno,
            @NotNull @Min(1) Integer oaId
    ) {
    }
}