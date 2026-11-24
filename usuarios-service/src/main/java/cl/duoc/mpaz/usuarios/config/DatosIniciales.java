package cl.duoc.mpaz.usuarios.config;

import cl.duoc.mpaz.usuarios.model.Usuario;
import cl.duoc.mpaz.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Carga las cuentas ficticias del proyecto.
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Value("${mpaz.demo.password.docente:1234}")
    private String passwordDocente;

    @Value("${mpaz.demo.password.utp:1234}")
    private String passwordUtp;

    @Value("${mpaz.demo.password.estudiante1:1234}")
    private String passwordEstudiante1;

    @Value("${mpaz.demo.password.estudiante2:1234}")
    private String passwordEstudiante2;

    public DatosIniciales(
            UsuarioRepository repository,
            PasswordEncoder encoder) {

        this.repository = repository;
        this.encoder = encoder;
    }

    // Evita duplicar usuarios al reiniciar el servicio.
    @Override
    @Transactional
    public void run(String... args) {

        if (repository.count() > 0) {
            return;
        }

        repository.save(new Usuario(
                "123456785",
                "Profesora Ana Soto",
                encoder.encode(passwordDocente),
                "DOCENTE",
                "3° Básico"
        ));

        repository.save(new Usuario(
                "134567899",
                "Jefa UTP Carla Rojas",
                encoder.encode(passwordUtp),
                "UTP",
                "3° Básico"
        ));

        repository.save(new Usuario(
                "111111111",
                "Estudiante Prueba 1",
                encoder.encode(passwordEstudiante1),
                "ESTUDIANTE",
                "3° Básico"
        ));

        repository.save(new Usuario(
                "222222222",
                "Estudiante Prueba 2",
                encoder.encode(passwordEstudiante2),
                "ESTUDIANTE",
                "3° Básico"
        ));
    }
}