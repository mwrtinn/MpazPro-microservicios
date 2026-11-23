# usuarios-service

Microservicio de usuarios y acceso por RUT (puerto 8081). Java 17, Spring Boot 3.3, Spring Data JPA, H2 y BCrypt.

## Cómo ejecutarlo
1. Abrir la carpeta con IntelliJ IDEA y esperar a que Maven descargue las dependencias.
2. Ejecutar `UsuariosServiceApplication`, o por consola: `mvn spring-boot:run`
3. Pruebas: `mvn test`

La base de datos queda en `./data/usuarios-db`. Consola: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/usuarios-db`, usuario `sa`, sin contraseña).

## Endpoints
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/auth/login` | Acceso por RUT, contraseña y perfil | 200, 400, 401 |
| GET | `/api/usuarios?rol=ESTUDIANTE` | Lista (el filtro es opcional) | 200 |
| GET | `/api/usuarios/{rut}` | Un usuario | 200, 404 |
| POST | `/api/usuarios` | Crea un usuario | 201, 400, 409 |
| PUT | `/api/usuarios/{rut}` | Actualiza (la contraseña es opcional) | 200, 400, 404 |
| DELETE | `/api/usuarios/{rut}` | Elimina | 204, 404 |

Los errores siempre tienen la forma `{ "error": "mensaje" }`.

## Usuarios de prueba (ficticios, contraseña 1234)
| Perfil | RUT |
|---|---|
| DOCENTE | 12.345.678-5 |
| UTP | 13.456.789-9 |
| ESTUDIANTE | 11.111.111-1 y 22.222.222-2 |