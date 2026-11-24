package cl.duoc.mpaz.aprendizaje.service;

import cl.duoc.mpaz.aprendizaje.model.*;
import cl.duoc.mpaz.aprendizaje.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Gestiona las respuestas, los intentos y los informes pedagógicos.
@Service
@Transactional
public class SeguimientoService {

    private final ObjetivoRepository objetivos;
    private final PreguntaRepository preguntas;
    private final RespuestaRepository respuestas;
    private final InformeRepository informes;

    public SeguimientoService(
            ObjetivoRepository objetivos,
            PreguntaRepository preguntas,
            RespuestaRepository respuestas,
            InformeRepository informes) {

        this.objetivos = objetivos;
        this.preguntas = preguntas;
        this.respuestas = respuestas;
        this.informes = informes;
    }

    // ---------- Respuestas de estudiantes ----------

    public List<Respuesta> listarRespuestas(
            String rut, boolean estudiante) {

        return estudiante
                ? respuestas.findByRutAlumno(rut)
                : respuestas.findAll();
    }

    public List<Respuesta> respuestasDeAlumno(
            String rutAlumno,
            String rutActual,
            boolean estudiante) {

        // Impide consultar los resultados de otro estudiante.
        if (estudiante && !rutActual.equals(rutAlumno)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar resultados de otro estudiante");
        }

        return respuestas.findByRutAlumno(rutAlumno);
    }

    // Registra y corrige una respuesta utilizando los datos del servidor.
    public Respuesta registrarRespuesta(
            String rutAlumno,
            int preguntaId,
            int intento,
            int elegida) {

        Pregunta pregunta = preguntas.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada"));

        Objetivo objetivo = buscarObjetivo(pregunta.oaId);

        if (!objetivo.asignado) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El objetivo no está asignado");
        }

        if (elegida < 0 || elegida > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Alternativa inválida");
        }

        int ultimo = respuestas.ultimoIntento(
                rutAlumno, pregunta.oaId, pregunta.tipo);

        // Controla la secuencia de los intentos.
        if (intento < 1
                || intento < ultimo
                || intento > ultimo + 1) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Número de intento inválido");
        }

        // El límite de intentos se aplica a la prueba final.
        if ("PRUEBA".equals(pregunta.tipo)
                && intento > objetivo.intentosPrueba) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya utilizaste los intentos permitidos");
        }

        // Evita respuestas duplicadas dentro del mismo intento.
        if (respuestas.existsByRutAlumnoAndPreguntaIdAndIntento(
                rutAlumno, preguntaId, intento)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La pregunta ya fue respondida en este intento");
        }

        Respuesta respuesta = new Respuesta();

        respuesta.rutAlumno = rutAlumno;
        respuesta.oaId = pregunta.oaId;
        respuesta.preguntaId = pregunta.id;
        respuesta.tipo = pregunta.tipo;
        respuesta.dificultad = pregunta.dificultad;
        respuesta.intento = intento;
        respuesta.elegida = elegida;
        respuesta.esCorrecta = elegida == pregunta.correcta;
        respuesta.fecha = System.currentTimeMillis();

        return respuestas.save(respuesta);
    }

    // Recupera la alternativa correcta después de responder.
    public int alternativaCorrecta(int preguntaId) {

        return preguntas.findById(preguntaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pregunta no encontrada"))
                .correcta;
    }

    // ---------- Control de intentos ----------

    public Map<String, Object> consultarIntentos(
            String rutAlumno,
            int oaId,
            String tipo) {

        Objetivo objetivo = buscarObjetivo(oaId);

        if (!"PRUEBA".equals(tipo)
                && !"DESAFIO".equals(tipo)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de evaluación inválido");
        }

        int usados = respuestas.ultimoIntento(
                rutAlumno, oaId, tipo);

        int permitidos = objetivo.intentosPrueba;

        int restantes = Math.max(permitidos - usados, 0);

        boolean disponible = objetivo.asignado
                && ("DESAFIO".equals(tipo) || restantes > 0);

        return Map.of(
                "rutAlumno", rutAlumno,
                "oaId", oaId,
                "tipo", tipo,
                "ultimoIntento", usados,
                "intentosPermitidos", permitidos,
                "intentosRestantes", restantes,
                "disponible", disponible
        );
    }

    // ---------- Informes pedagógicos ----------

    public List<Informe> listarInformes() {
        return informes.findAll();
    }

    public Informe buscarInforme(String clave) {

        return informes.findById(clave)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Informe no encontrado"));
    }

    // Genera un informe a partir de las respuestas registradas.
    public Informe generarInforme(
            String rutAlumno, int oaId) {

        buscarObjetivo(oaId);

        if (rutAlumno == null || rutAlumno.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El RUT del estudiante es obligatorio");
        }

        List<Respuesta> lista =
                respuestas.findByRutAlumnoAndOaId(rutAlumno, oaId);

        Informe informe = new Informe();

        informe.clave = rutAlumno + "-" + oaId;
        informe.rutAlumno = rutAlumno;
        informe.oaId = oaId;
        informe.fecha = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Todo informe generado debe ser validado por el docente.
        informe.validado = false;

        if (lista.isEmpty()) {

            informe.rendimiento = 0;
            informe.fortalezas = "Sin actividad registrada.";
            informe.refuerzo = "Sin actividad registrada.";

            return informes.save(informe);
        }

        List<Respuesta> pruebas = lista.stream()
                .filter(r -> "PRUEBA".equals(r.tipo))
                .toList();

        List<Respuesta> base = lista;

        // Prioriza el último intento de la prueba final.
        if (!pruebas.isEmpty()) {

            int ultimo = pruebas.stream()
                    .mapToInt(r -> r.intento)
                    .max()
                    .orElse(0);

            base = pruebas.stream()
                    .filter(r -> r.intento == ultimo)
                    .toList();
        }

        informe.rendimiento = porcentaje(base);

        List<String> fortalezas = new ArrayList<>();
        List<String> refuerzos = new ArrayList<>();

        // Analiza el rendimiento por nivel de dificultad.
        Map<Integer, List<Respuesta>> porNivel =
                lista.stream().collect(Collectors.groupingBy(
                        r -> r.dificultad,
                        TreeMap::new,
                        Collectors.toList()));

        for (Map.Entry<Integer, List<Respuesta>> nivel
                : porNivel.entrySet()) {

            int resultado = porcentaje(nivel.getValue());

            String etiqueta = "preguntas de nivel "
                    + nivel.getKey()
                    + " (" + resultado + "% correctas)";

            if (resultado >= 70) {
                fortalezas.add(etiqueta);
            } else if (resultado < 50) {
                refuerzos.add(etiqueta);
            }
        }

        informe.fortalezas = fortalezas.isEmpty()
                ? "Aún sin fortalezas claras."
                : String.join("; ", fortalezas);

        informe.refuerzo = refuerzos.isEmpty()
                ? "Sin contenidos que requieran refuerzo por ahora."
                : String.join("; ", refuerzos);

        return informes.save(informe);
    }

    // Registra la validación profesional del docente.
    public Informe validarInforme(String clave) {

        Informe informe = buscarInforme(clave);

        informe.validado = true;

        return informes.save(informe);
    }

    // ---------- Métodos auxiliares ----------

    private Objetivo buscarObjetivo(int oaId) {

        return objetivos.findById(oaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Objetivo no encontrado"));
    }

    // Calcula el porcentaje de respuestas correctas.
    private int porcentaje(List<Respuesta> lista) {

        if (lista.isEmpty()) {
            return 0;
        }

        long correctas = lista.stream()
                .filter(r -> r.esCorrecta)
                .count();

        return (int) (correctas * 100 / lista.size());
    }
}