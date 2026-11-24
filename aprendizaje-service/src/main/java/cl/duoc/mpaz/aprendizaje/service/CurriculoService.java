package cl.duoc.mpaz.aprendizaje.service;

import cl.duoc.mpaz.aprendizaje.dto.PreguntaResponse;
import cl.duoc.mpaz.aprendizaje.model.*;
import cl.duoc.mpaz.aprendizaje.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

// Gestiona el currículo y los recursos educativos.
@Service
@Transactional
public class CurriculoService {

    private final ObjetivoRepository objetivos;
    private final ContenidoRepository contenidos;
    private final PreguntaRepository preguntas;
    private final RespuestaRepository respuestas;
    private final InformeRepository informes;

    public CurriculoService(
            ObjetivoRepository objetivos,
            ContenidoRepository contenidos,
            PreguntaRepository preguntas,
            RespuestaRepository respuestas,
            InformeRepository informes) {

        this.objetivos = objetivos;
        this.contenidos = contenidos;
        this.preguntas = preguntas;
        this.respuestas = respuestas;
        this.informes = informes;
    }

    // ---------- Objetivos de aprendizaje ----------

    public List<Objetivo> listarObjetivos(boolean estudiante) {
        return estudiante
                ? objetivos.findByAsignadoTrue()
                : objetivos.findAll();
    }

    public Objetivo buscarObjetivo(int id) {
        return objetivos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Objetivo no encontrado"));
    }

    public Objetivo consultarObjetivo(
            int id, boolean estudiante) {

        Objetivo objetivo = buscarObjetivo(id);

        if (estudiante && !objetivo.asignado) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Objetivo no asignado");
        }

        return objetivo;
    }

    public Objetivo crearObjetivo(Objetivo objetivo) {
        objetivo.id = null;
        validarObjetivo(objetivo);
        return objetivos.save(objetivo);
    }

    public Objetivo actualizarObjetivo(
            int id, Objetivo datos) {

        buscarObjetivo(id);
        datos.id = id;
        validarObjetivo(datos);

        return objetivos.save(datos);
    }

    // Evita eliminar objetivos que tienen registros asociados.
    public void eliminarObjetivo(int id) {

        buscarObjetivo(id);

        if (!contenidos.findByOaId(id).isEmpty()
                || !preguntas.findByOaId(id).isEmpty()
                || respuestas.existsByOaId(id)
                || !informes.findByOaId(id).isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El objetivo tiene registros asociados");
        }

        objetivos.deleteById(id);
    }

    // ---------- Contenidos educativos ----------

    public List<Contenido> listarContenidos(
            Integer oaId, boolean estudiante) {

        List<Contenido> lista = oaId == null
                ? contenidos.findAll()
                : contenidos.findByOaId(oaId);

        if (!estudiante) {
            return lista;
        }

        // El estudiante solo recibe material aprobado y asignado.
        return lista.stream()
                .filter(c -> "APROBADO".equals(c.estado))
                .filter(c -> objetivos.findById(c.oaId)
                        .map(o -> o.asignado)
                        .orElse(false))
                .toList();
    }

    public Contenido buscarContenido(int id) {
        return contenidos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contenido no encontrado"));
    }

    public Contenido consultarContenido(
            int id, boolean estudiante) {

        Contenido contenido = buscarContenido(id);

        if (estudiante) {
            consultarObjetivo(contenido.oaId, true);

            if (!"APROBADO".equals(contenido.estado)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Contenido no aprobado");
            }
        }

        return contenido;
    }

    public Contenido crearContenido(Contenido contenido) {

        validarContenido(contenido);

        contenido.id = null;
        contenido.estado = "PENDIENTE";
        contenido.observacionUtp = "";

        return contenidos.save(contenido);
    }

    public Contenido actualizarContenido(
            int id, Contenido datos) {

        buscarContenido(id);
        validarContenido(datos);

        datos.id = id;

        // El contenido editado debe volver a revisión de UTP.
        datos.estado = "PENDIENTE";
        datos.observacionUtp = "";

        return contenidos.save(datos);
    }

    // Permite que UTP apruebe o rechace un contenido.
    public Contenido revisarContenido(
            int id, String estado, String observacion) {

        Contenido contenido = buscarContenido(id);

        if (estado == null
                || !Set.of("APROBADO", "RECHAZADO")
                .contains(estado)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de revisión inválido");
        }

        contenido.estado = estado;
        contenido.observacionUtp =
                observacion == null ? "" : observacion;

        return contenidos.save(contenido);
    }

    public void eliminarContenido(int id) {
        buscarContenido(id);
        contenidos.deleteById(id);
    }

    // ---------- Preguntas y evaluaciones ----------

    public List<PreguntaResponse> listarPreguntas(
            Integer oaId, String tipo, boolean estudiante) {

        List<Pregunta> lista;

        if (oaId != null && tipo != null) {
            lista = preguntas
                    .findByOaIdAndTipoOrderByDificultadAscIdAsc(
                            oaId, tipo);
        } else if (oaId != null) {
            lista = preguntas.findByOaId(oaId);
        } else {
            lista = preguntas.findAll();
        }

        return lista.stream()
                .filter(p -> tipo == null || tipo.equals(p.tipo))
                .filter(p -> !estudiante
                        || objetivos.findById(p.oaId)
                        .map(o -> o.asignado)
                        .orElse(false))
                .map(p -> PreguntaResponse.desde(p, estudiante))
                .toList();
    }

    public Pregunta buscarPregunta(int id) {
        return preguntas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada"));
    }

    public PreguntaResponse consultarPregunta(
            int id, boolean estudiante) {

        Pregunta pregunta = buscarPregunta(id);

        consultarObjetivo(pregunta.oaId, estudiante);

        return PreguntaResponse.desde(pregunta, estudiante);
    }

    public Pregunta crearPregunta(Pregunta pregunta) {

        validarPregunta(pregunta);

        pregunta.id = null;

        return preguntas.save(pregunta);
    }

    public Pregunta actualizarPregunta(
            int id, Pregunta datos) {

        buscarPregunta(id);
        validarPregunta(datos);

        // Protege la integridad de los resultados anteriores.
        if (respuestas.existsByPreguntaId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La pregunta ya tiene respuestas registradas");
        }

        datos.id = id;

        return preguntas.save(datos);
    }

    public void eliminarPregunta(int id) {

        buscarPregunta(id);

        if (respuestas.existsByPreguntaId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La pregunta tiene respuestas asociadas");
        }

        preguntas.deleteById(id);
    }

    // ---------- Validaciones generales ----------

    private void validarObjetivo(Objetivo objetivo) {

        obligatorio(objetivo.asignatura, "Asignatura");
        obligatorio(objetivo.unidad, "Unidad");
        obligatorio(objetivo.codigo, "Código");
        obligatorio(objetivo.eje, "Eje");
        obligatorio(objetivo.descripcion, "Descripción");
        obligatorio(objetivo.referencia, "Referencia");

        if (objetivo.intentosPrueba < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe permitir al menos un intento");
        }

        // El MVP permite una sola unidad por asignatura.
        boolean otraUnidad = objetivos.findAll().stream()
                .anyMatch(o ->
                        !o.id.equals(objetivo.id)
                                && o.asignatura.equalsIgnoreCase(
                                objetivo.asignatura)
                                && !o.unidad.equalsIgnoreCase(
                                objetivo.unidad));

        if (otraUnidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se permite una unidad por asignatura");
        }
    }

    private void validarContenido(Contenido contenido) {

        buscarObjetivo(contenido.oaId);

        obligatorio(contenido.titulo, "Título");
        obligatorio(contenido.explicacion, "Explicación");
        obligatorio(contenido.ejemplo, "Ejemplo");
        obligatorio(contenido.icono, "Icono");
    }

    private void validarPregunta(Pregunta pregunta) {

        buscarObjetivo(pregunta.oaId);

        obligatorio(pregunta.tipo, "Tipo");
        obligatorio(pregunta.enunciado, "Enunciado");
        obligatorio(pregunta.opcionA, "Alternativa A");
        obligatorio(pregunta.opcionB, "Alternativa B");
        obligatorio(pregunta.opcionC, "Alternativa C");

        if (!Set.of("DESAFIO", "PRUEBA")
                .contains(pregunta.tipo)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de pregunta inválido");
        }

        if (pregunta.dificultad < 1
                || pregunta.dificultad > 3) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La dificultad debe estar entre 1 y 3");
        }

        if (pregunta.correcta < 0
                || pregunta.correcta > 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Alternativa correcta inválida");
        }
    }

    // Comprueba que los campos obligatorios tengan contenido.
    private void obligatorio(String valor, String campo) {

        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    campo + " es obligatorio");
        }
    }
}