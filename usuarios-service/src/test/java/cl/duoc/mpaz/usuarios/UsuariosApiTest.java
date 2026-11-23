package cl.duoc.mpaz.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Usa una base H2 en memoria para no tocar la base real
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:usuarios-test;DB_CLOSE_DELAY=-1")
class UsuariosApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginCorrectoDevuelveElUsuarioSinContrasena() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11.111.111-1\",\"password\":\"1234\",\"rol\":\"ESTUDIANTE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("111111111"))
                .andExpect(jsonPath("$.rol").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"111111111\",\"password\":\"mala\",\"rol\":\"ESTUDIANTE\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginConPerfilDistintoDevuelve401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"111111111\",\"password\":\"1234\",\"rol\":\"DOCENTE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSinCamposDevuelve400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"\",\"password\":\"\",\"rol\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").exists());
    }

    @Test
    void listarEstudiantesFiltraPorRol() throws Exception {
        mvc.perform(get("/api/usuarios").param("rol", "ESTUDIANTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void crearYEliminarUnUsuario() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"10.000.004-0\",\"nombre\":\"Nuevo Estudiante\",\"password\":\"abcd\",\"rol\":\"ESTUDIANTE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rut").value("100000040"));

        mvc.perform(delete("/api/usuarios/100000040")).andExpect(status().isNoContent());
        mvc.perform(delete("/api/usuarios/100000040")).andExpect(status().isNotFound());
    }

    @Test
    void crearConRutInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"11.111.111-9\",\"nombre\":\"Nombre Valido\",\"password\":\"abcd\",\"rol\":\"ESTUDIANTE\"}"))
                .andExpect(status().isBadRequest());
    }
}