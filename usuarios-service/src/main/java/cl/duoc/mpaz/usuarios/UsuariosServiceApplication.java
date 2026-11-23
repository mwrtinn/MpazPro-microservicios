package cl.duoc.mpaz.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada del microservicio de usuarios (puerto 8081). */
@SpringBootApplication
public class UsuariosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}