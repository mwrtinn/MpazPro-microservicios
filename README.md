# MpazPro - Microservicios

Microservicios Spring Boot (Java 17, Maven) de MpazPro, la app de apoyo didáctico de la Escuela Escritora Marcela Paz (DSY1105 - Duoc UC). La app móvil está en el repositorio `MpazPro`.

## Microservicios
| Servicio | Puerto | Contenido |
|---|---|---|
| `usuarios-service` | 8081 | Acceso por RUT y CRUD de usuarios |
| `curriculo-service` | 8082 | Objetivos, contenidos (aprobación UTP) y preguntas |
| `seguimiento-service` | 8083 | Respuestas e informes pedagógicos |

Cada carpeta es un proyecto independiente, con su `pom.xml`, su base de datos y su README. Los endpoints y el JSON de los tres servicios están en [`CONTRATO_API.md`](CONTRATO_API.md).

## Cómo ejecutar
1. Abrir la carpeta de un servicio con IntelliJ IDEA y esperar a que Maven descargue las dependencias.
2. Ejecutar su clase `...Application`, o dentro de la carpeta: `mvn spring-boot:run`
3. Pruebas: `mvn test`

Todos los datos son ficticios.