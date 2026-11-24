package cl.duoc.mpaz.aprendizaje.config;

import cl.duoc.mpaz.aprendizaje.model.Contenido;
import cl.duoc.mpaz.aprendizaje.model.Objetivo;
import cl.duoc.mpaz.aprendizaje.model.Pregunta;
import cl.duoc.mpaz.aprendizaje.repository.ContenidoRepository;
import cl.duoc.mpaz.aprendizaje.repository.ObjetivoRepository;
import cl.duoc.mpaz.aprendizaje.repository.PreguntaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Carga los datos educativos ficticios del proyecto Android original.
@Component
public class DatosIniciales implements ApplicationRunner {

    private final ObjetivoRepository objetivos;
    private final ContenidoRepository contenidos;
    private final PreguntaRepository preguntas;

    private static final String REF =
            "Currículum Nacional - curriculumnacional.mineduc.cl (verificar código oficial)";

    public DatosIniciales(
            ObjetivoRepository objetivos,
            ContenidoRepository contenidos,
            PreguntaRepository preguntas) {

        this.objetivos = objetivos;
        this.contenidos = contenidos;
        this.preguntas = preguntas;
    }

    // Carga los datos una sola vez para evitar duplicados.
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (objetivos.count() > 0) {
            return;
        }

        cargarMatematica();
        cargarLenguaje();
        cargarCiencias();
        cargarHistoria();
    }

    // ---------- Matemática ----------

    private void cargarMatematica() {

        int oa = objetivo(
                "Matemática",
                "Unidad 1: Números hasta el 1.000",
                "MA03 OA 01",
                "Números y operaciones",
                "Contar números del 0 al 1.000 de 2 en 2, de 5 en 5, de 10 en 10 y de 100 en 100.",
                true
        );

        contenido(oa,
                "Contar de 5 en 5 y de 10 en 10",
                "Contar de 5 en 5 es sumar 5 cada vez. Empezamos en 0 y vamos dando saltos: 5, 10, 15, 20. Contar de 10 en 10 es igual, pero con saltos de 10.",
                "Si cuento de 10 en 10: 10, 20, 30, 40, 50.",
                "🔢", "APROBADO");

        contenido(oa,
                "Contar de 100 en 100",
                "Para contar de 100 en 100 sumamos 100 cada vez: 100, 200, 300, 400. ¡Así llegamos rápido al 1.000!",
                "100, 200, 300 y ahora sigue el 400.",
                "💯", "PENDIENTE");

        pregunta(oa, "DESAFIO", 1,
                "Cuenta de 2 en 2: 2, 4, 6, 8, ¿qué número sigue?",
                "9", "10", "12", 1);

        pregunta(oa, "DESAFIO", 2,
                "Cuenta de 5 en 5: 5, 10, 15, ¿qué número sigue?",
                "20", "25", "30", 0);

        pregunta(oa, "PRUEBA", 1,
                "Cuenta de 10 en 10: 10, 20, 30, 40, ¿qué número sigue?",
                "45", "50", "60", 1);

        pregunta(oa, "PRUEBA", 2,
                "Cuenta de 100 en 100: 100, 200, 300, ¿qué número sigue?",
                "400", "350", "500", 0);
    }

    // ---------- Lenguaje y Comunicación ----------

    private void cargarLenguaje() {

        int oa = objetivo(
                "Lenguaje y Comunicación",
                "Unidad 1: Leemos cuentos",
                "LE03 OA 04",
                "Lectura",
                "Comprender textos narrativos breves: reconocer personajes, lugares y lo que ocurre.",
                true
        );

        contenido(oa,
                "Leer un cuento corto",
                "Para entender un cuento busca quiénes son los personajes, dónde ocurre y qué pasa primero y después.",
                "«Ana ayudó a Toto». Los personajes son Ana y Toto.",
                "📖", "APROBADO");

        pregunta(oa, "DESAFIO", 1,
                "Lee: «Ana tiene un perro llamado Toto». ¿Cómo se llama el perro?",
                "Ana", "Toto", "Perro", 1);

        pregunta(oa, "DESAFIO", 2,
                "Lee: «Llovía mucho, así que Ana se puso su paraguas». ¿Por qué se puso el paraguas?",
                "Llovía mucho", "Hacía calor", "Era de noche", 0);

        pregunta(oa, "PRUEBA", 1,
                "Lee: «Toto corrió al jardín y jugó con la pelota». ¿Dónde jugó Toto?",
                "En la casa", "En el jardín", "En la escuela", 1);

        pregunta(oa, "PRUEBA", 2,
                "Lee: «Ana estaba triste porque perdió su pelota». ¿Cómo se sentía Ana?",
                "Alegre", "Enojada", "Triste", 2);
    }

    // ---------- Ciencias Naturales ----------

    private void cargarCiencias() {

        int oa = objetivo(
                "Ciencias Naturales",
                "Unidad 1: Las plantas",
                "CN03 OA 01",
                "Ciencias de la vida",
                "Reconocer qué necesitan las plantas para vivir y qué función cumplen sus partes.",
                false
        );

        contenido(oa,
                "Lo que necesitan las plantas",
                "Las plantas necesitan agua, luz y aire para crecer. Las raíces toman el agua del suelo y las hojas reciben la luz.",
                "Una planta junto a la ventana recibe luz y crece mejor.",
                "🌱", "APROBADO");

        pregunta(oa, "DESAFIO", 1,
                "¿Qué necesita una planta para crecer?",
                "Solo juguetes", "Agua y luz", "Hielo", 1);

        pregunta(oa, "DESAFIO", 2,
                "Si una planta no recibe luz por muchos días, ¿qué le puede pasar?",
                "Crece más rápido", "Se pone más verde", "Se debilita", 2);

        pregunta(oa, "PRUEBA", 1,
                "¿Qué parte de la planta toma el agua del suelo?",
                "Raíz", "Hoja", "Flor", 0);

        pregunta(oa, "PRUEBA", 2,
                "¿Qué parte de la planta recibe la luz del sol?",
                "Raíz", "Hoja", "Semilla", 1);
    }

    // ---------- Historia y Geografía ----------

    private void cargarHistoria() {

        int oa = objetivo(
                "Historia, Geografía y Ciencias Sociales",
                "Unidad 1: Nos ubicamos en el mapa",
                "HI03 OA 01",
                "Geografía",
                "Usar mapas y puntos cardinales para ubicar lugares.",
                false
        );

        contenido(oa,
                "Los puntos cardinales",
                "Los puntos cardinales nos ayudan a orientarnos: Norte, Sur, Este y Oeste.",
                "El sol sale por el Este y se esconde por el Oeste.",
                "🧭", "APROBADO");

        pregunta(oa, "DESAFIO", 1,
                "¿Cuántos puntos cardinales hay?",
                "2", "3", "4", 2);

        pregunta(oa, "DESAFIO", 2,
                "¿Por dónde sale el sol por la mañana?",
                "Este", "Oeste", "Norte", 0);

        pregunta(oa, "PRUEBA", 1,
                "¿Qué nos muestra un mapa?",
                "Recetas", "Lugares y su ubicación", "Canciones", 1);

        pregunta(oa, "PRUEBA", 2,
                "Los puntos cardinales son Norte, Sur, Este y...",
                "Centro", "Arriba", "Oeste", 2);
    }

    // ---------- Métodos auxiliares ----------

    // Guarda un objetivo y devuelve el ID generado por la base de datos.
    private int objetivo(
            String asignatura,
            String unidad,
            String codigo,
            String eje,
            String descripcion,
            boolean asignado) {

        Objetivo objetivo = new Objetivo();

        objetivo.asignatura = asignatura;
        objetivo.unidad = unidad;
        objetivo.codigo = codigo;
        objetivo.eje = eje;
        objetivo.descripcion = descripcion;
        objetivo.referencia = REF;
        objetivo.asignado = asignado;
        objetivo.intentosPrueba = 2;

        return objetivos.save(objetivo).id;
    }

    // Guarda un contenido asociado a su objetivo de aprendizaje.
    private void contenido(
            int oaId,
            String titulo,
            String explicacion,
            String ejemplo,
            String icono,
            String estado) {

        Contenido contenido = new Contenido();

        contenido.oaId = oaId;
        contenido.titulo = titulo;
        contenido.explicacion = explicacion;
        contenido.ejemplo = ejemplo;
        contenido.icono = icono;
        contenido.estado = estado;
        contenido.observacionUtp = "";
        contenido.imagenPath = null;

        contenidos.save(contenido);
    }

    // Guarda una pregunta con sus alternativas y respuesta correcta.
    private void pregunta(
            int oaId,
            String tipo,
            int dificultad,
            String enunciado,
            String a,
            String b,
            String c,
            int correcta) {

        Pregunta pregunta = new Pregunta();

        pregunta.oaId = oaId;
        pregunta.tipo = tipo;
        pregunta.dificultad = dificultad;
        pregunta.enunciado = enunciado;
        pregunta.opcionA = a;
        pregunta.opcionB = b;
        pregunta.opcionC = c;
        pregunta.correcta = correcta;

        preguntas.save(pregunta);
    }
}