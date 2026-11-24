package cl.duoc.mpaz.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Utiliza una base H2 en memoria para no modificar los datos reales.
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuarios-test;DB_CLOSE_DELAY=-1",
        "mpaz.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class UsuariosApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // Obtiene un token JWT mediante el endpoint de autenticación.
    private String token(String rut, String rol) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "rut", rut,
                                "password", "1234",
                                "rol", rol))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return mapper.readTree(respuesta).get("token").asText();
    }

    // Pruebas de autenticación.

    @Test
    void loginCorrectoDevuelveElUsuarioSinContrasena() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "11.111.111-1",
                                    "password": "1234",
                                    "rol": "ESTUDIANTE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("111111111"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "111111111",
                                    "password": "mala",
                                    "rol": "ESTUDIANTE"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginConPerfilDistintoDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "111111111",
                                    "password": "1234",
                                    "rol": "DOCENTE"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinCamposDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "",
                                    "password": "",
                                    "rol": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }

    // Pruebas de gestión de usuarios con permisos autorizados.

    @Test
    void listarEstudiantesFiltraPorRol() throws Exception {
        mvc.perform(get("/api/usuarios")
                        .param("rol", "ESTUDIANTE")
                        .header("Authorization",
                                "Bearer " + token("123456785", "DOCENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void crearYEliminarUnUsuario() throws Exception {

        String auth = "Bearer " + token("134567899", "UTP");

        // Crea un usuario temporal.
        mvc.perform(post("/api/usuarios")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "10.000.004-0",
                                    "nombre": "Nuevo Estudiante",
                                    "password": "abcd",
                                    "rol": "ESTUDIANTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("100000040"));

        try {

            // Consulta el usuario creado.
            mvc.perform(get("/api/usuarios/100000040")
                            .header("Authorization", auth))
                    .andExpect(status().isOk());

            // Actualiza los datos del usuario.
            mvc.perform(put("/api/usuarios/100000040")
                            .header("Authorization", auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "nombre": "Usuario Actualizado",
                                        "rol": "ESTUDIANTE"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre")
                            .value("Usuario Actualizado"));

        } finally {

            // Elimina el usuario temporal al finalizar la prueba.
            mvc.perform(delete("/api/usuarios/100000040")
                            .header("Authorization", auth))
                    .andExpect(status().isNoContent());
        }

        mvc.perform(delete("/api/usuarios/100000040")
                        .header("Authorization", auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearConRutInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .header("Authorization",
                                "Bearer " + token("134567899", "UTP"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "rut": "11.111.111-9",
                                    "nombre": "Nombre Valido",
                                    "password": "abcd",
                                    "rol": "ESTUDIANTE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Pruebas de seguridad y restricciones por perfil.

    @Test
    void listarSinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void estudianteNoPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios")
                        .header("Authorization",
                                "Bearer " + token("111111111", "ESTUDIANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void docenteNoPuedeEliminarUsuarios() throws Exception {
        mvc.perform(delete("/api/usuarios/100000040")
                        .header("Authorization",
                                "Bearer " + token("123456785", "DOCENTE")))
                .andExpect(status().isForbidden());
    }

    // Comprueba que un token con la firma modificada sea rechazado.
    @Test
    void tokenAlteradoDevuelve401() throws Exception {

        String original = token("123456785", "DOCENTE");

        int inicioFirma = original.lastIndexOf('.') + 1;

        char reemplazo = original.charAt(inicioFirma) == 'A'
                ? 'B' : 'A';

        String alterado = original.substring(0, inicioFirma)
                + reemplazo
                + original.substring(inicioFirma + 1);

        mvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    // Genera un token vencido utilizando la clave exclusiva de pruebas.
    @Test
    void tokenVencidoDevuelve401() throws Exception {

        byte[] clave = Base64.getDecoder().decode(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(
                        new SecretKeySpec(clave, "HmacSHA256")));

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mpaz-usuarios-service")
                .subject("123456785")
                .audience(List.of("mpaz-api"))
                .issuedAt(ahora.minusSeconds(7200))
                .expiresAt(ahora.minusSeconds(300))
                .claim("rol", "DOCENTE")
                .build();

        String vencido = encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        mvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }
}