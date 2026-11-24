package cl.duoc.mpaz.aprendizaje;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de aprendizaje.
@SpringBootApplication
public class AprendizajeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                AprendizajeServiceApplication.class, args);
    }
}