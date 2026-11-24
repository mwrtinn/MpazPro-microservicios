package cl.duoc.mpaz.aprendizaje;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Pruebas de integración del microservicio de aprendizaje.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:aprendizaje-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "mpaz.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class AprendizajeApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // Clave exclusiva de pruebas automatizadas.
    private static final String CLAVE_TEST =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    // Genera tokens de prueba sin depender de usuarios-service.
    private String token(String rut, String rol) {

        byte[] clave = Base64.getDecoder().decode(CLAVE_TEST);

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(
                        new SecretKeySpec(clave, "HmacSHA256")));

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mpaz-usuarios-service")
                .subject(rut)
                .audience(List.of("mpaz-api"))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("rol", rol)
                .build();

        return "Bearer " + encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();
    }

    private String docente() {
        return token("123456785", "DOCENTE");
    }

    private String alumno() {
        return token("111111111", "ESTUDIANTE");
    }

    private String utp() {
        return token("134567899", "UTP");
    }

    // ---------- Seguridad y consultas de currículo ----------

    @Test
    void consultaSinTokenDevuelve401() throws Exception {

        mvc.perform(get("/api/objetivos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void docenteConsultaTodosLosObjetivos() throws Exception {

        mvc.perform(get("/api/objetivos")
                        .header("Authorization", docente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void estudianteSoloConsultaObjetivosAsignados() throws Exception {

        mvc.perform(get("/api/objetivos")
                        .header("Authorization", alumno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/objetivos/3")
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());
    }

    @Test
    void estudianteSoloConsultaContenidosAprobados() throws Exception {

        mvc.perform(get("/api/contenidos")
                        .header("Authorization", alumno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/contenidos/2")
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());
    }

    @Test
    void estudianteNoRecibeAlternativaCorrecta() throws Exception {

        mvc.perform(get("/api/preguntas")
                        .header("Authorization", alumno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[*].correcta").doesNotExist());

        mvc.perform(get("/api/preguntas/1")
                        .header("Authorization", docente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correcta").value(1));
    }

    @Test
    void estudianteNoPuedeCrearObjetivos() throws Exception {

        mvc.perform(post("/api/objetivos")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---------- Operaciones CRUD ----------

    @Test
    void docenteRealizaCrudDeObjetivos() throws Exception {

        String datos = """
                {
                    "asignatura": "Matemática",
                    "unidad": "Unidad 1: Números hasta el 1.000",
                    "codigo": "OA TEST",
                    "eje": "Números y operaciones",
                    "descripcion": "Objetivo temporal",
                    "referencia": "Referencia de prueba",
                    "asignado": false,
                    "intentosPrueba": 2
                }
                """;

        String respuesta = mvc.perform(post("/api/objetivos")
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = mapper.readTree(respuesta).get("id").asInt();

        mvc.perform(get("/api/objetivos/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isOk());

        mvc.perform(put("/api/objetivos/" + id)
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos.replace(
                                "Objetivo temporal",
                                "Objetivo actualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion")
                        .value("Objetivo actualizado"));

        mvc.perform(delete("/api/objetivos/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/objetivos/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isNotFound());
    }

    @Test
    void contenidoRequiereAprobacionDeUtp() throws Exception {

        String datos = """
                {
                    "oaId": 1,
                    "titulo": "Contenido de prueba",
                    "explicacion": "Explicación temporal",
                    "ejemplo": "Ejemplo temporal",
                    "icono": "123"
                }
                """;

        String respuesta = mvc.perform(post("/api/contenidos")
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado")
                        .value("PENDIENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = mapper.readTree(respuesta).get("id").asInt();

        mvc.perform(get("/api/contenidos/" + id)
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());

        String revision = """
                {
                    "estado": "APROBADO",
                    "observacionUtp": "Contenido revisado"
                }
                """;

        // El docente no puede aprobar su propio contenido.
        mvc.perform(patch("/api/contenidos/" + id + "/estado")
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revision))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/contenidos/" + id + "/estado")
                        .header("Authorization", utp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revision))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado")
                        .value("APROBADO"));

        mvc.perform(get("/api/contenidos/" + id)
                        .header("Authorization", alumno()))
                .andExpect(status().isOk());

        // Si el docente edita el contenido, vuelve a revisión.
        mvc.perform(put("/api/contenidos/" + id)
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos.replace(
                                "Contenido de prueba",
                                "Contenido actualizado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado")
                        .value("PENDIENTE"));

        mvc.perform(delete("/api/contenidos/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isNoContent());
    }

    @Test
    void docenteRealizaCrudDePreguntas() throws Exception {

        String datos = """
                {
                    "oaId": 1,
                    "tipo": "DESAFIO",
                    "dificultad": 1,
                    "enunciado": "Cuánto es 2 + 2",
                    "opcionA": "4",
                    "opcionB": "5",
                    "opcionC": "6",
                    "correcta": 0
                }
                """;

        String respuesta = mvc.perform(post("/api/preguntas")
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int id = mapper.readTree(respuesta).get("id").asInt();

        mvc.perform(get("/api/preguntas/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isOk());

        mvc.perform(put("/api/preguntas/" + id)
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos.replace(
                                "Cuánto es 2 + 2",
                                "Pregunta actualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado")
                        .value("Pregunta actualizada"));

        mvc.perform(delete("/api/preguntas/" + id)
                        .header("Authorization", docente()))
                .andExpect(status().isNoContent());
    }

    // ---------- Respuestas e intentos ----------

    // Construye una solicitud para registrar una respuesta.
    private String respuesta(
            int preguntaId,
            int intento,
            int elegida) throws Exception {

        return mapper.writeValueAsString(Map.of(
                "preguntaId", preguntaId,
                "intento", intento,
                "elegida", elegida
        ));
    }

    @Test
    void servidorCorrigeYRechazaRespuestasDuplicadas() throws Exception {

        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(1, 1, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutAlumno")
                        .value("111111111"))
                .andExpect(jsonPath("$.esCorrecta").value(true));

        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(1, 1, 1)))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(2, 1, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esCorrecta").value(false));
    }

    @Test
    void estudianteNoPuedeSuperarLimiteDeIntentos() throws Exception {

        // Completa los dos intentos permitidos de la prueba.
        for (int intento = 1; intento <= 2; intento++) {

            mvc.perform(post("/api/respuestas")
                            .header("Authorization", alumno())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(respuesta(3, intento, 1)))
                    .andExpect(status().isCreated());

            mvc.perform(post("/api/respuestas")
                            .header("Authorization", alumno())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(respuesta(4, intento, 0)))
                    .andExpect(status().isCreated());
        }

        // El tercer intento debe ser rechazado.
        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(3, 3, 1)))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/intentos")
                        .param("oaId", "1")
                        .param("tipo", "PRUEBA")
                        .header("Authorization", alumno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intentosRestantes")
                        .value(0))
                .andExpect(jsonPath("$.disponible")
                        .value(false));
    }

    @Test
    void estudianteNoConsultaResultadosDeOtroAlumno() throws Exception {

        mvc.perform(get("/api/respuestas/alumno/222222222")
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/intentos")
                        .param("rut", "222222222")
                        .param("oaId", "1")
                        .param("tipo", "PRUEBA")
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());
    }

    // ---------- Informes pedagógicos ----------

    @Test
    void docenteGeneraYValidaInforme() throws Exception {

        // Registra una respuesta correcta y otra incorrecta.
        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(1, 1, 1)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/respuestas")
                        .header("Authorization", alumno())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(respuesta(2, 1, 2)))
                .andExpect(status().isCreated());

        String datos = """
                {
                    "rutAlumno": "111111111",
                    "oaId": 1
                }
                """;

        mvc.perform(put("/api/informes/111111111-1")
                        .header("Authorization", docente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(datos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rendimiento").value(50))
                .andExpect(jsonPath("$.fortalezas").isNotEmpty())
                .andExpect(jsonPath("$.refuerzo").isNotEmpty())
                .andExpect(jsonPath("$.validado").value(false));

        mvc.perform(patch("/api/informes/111111111-1/validar")
                        .header("Authorization", docente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validado").value(true));
    }

    @Test
    void informesSonExclusivosDelDocente() throws Exception {

        mvc.perform(get("/api/informes")
                        .header("Authorization", alumno()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/informes")
                        .header("Authorization", utp()))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/informes")
                        .header("Authorization", docente()))
                .andExpect(status().isOk());
    }
}