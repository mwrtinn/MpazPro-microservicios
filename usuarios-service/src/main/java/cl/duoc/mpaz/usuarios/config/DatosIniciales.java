package cl.duoc.mpaz.usuarios.config;

import cl.duoc.mpaz.usuarios.model.Usuario;
import cl.duoc.mpaz.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Carga los usuarios ficticios del caso la primera vez que se inicia el servicio. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        // Todos usan la contraseña 1234; los RUT tienen dígito verificador válido
        String clave = encoder.encode("1234");
        repository.save(new Usuario("123456785", "Profesora Ana Soto", clave, "DOCENTE", "3° Básico"));
        repository.save(new Usuario("134567899", "Jefa UTP Carla Rojas", clave, "UTP", "3° Básico"));
        repository.save(new Usuario("111111111", "Estudiante Prueba 1", clave, "ESTUDIANTE", "3° Básico"));
        repository.save(new Usuario("222222222", "Estudiante Prueba 2", clave, "ESTUDIANTE", "3° Básico"));
    }
}