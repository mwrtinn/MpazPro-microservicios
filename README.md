# MPAZ PRO 3.º - Microservicios

Proyecto de Aplicaciones Móviles - Duoc UC
Evaluación Parcial 4 - DSY1105

## Descripción

MPAZ PRO es una aplicación educativa orientada a estudiantes
de tercero básico de la Escuela Escritora Marcela Paz.

Permite gestionar objetivos de aprendizaje, contenidos
educativos, desafíos, evaluaciones e informes pedagógicos.

El sistema utiliza exclusivamente usuarios y datos ficticios.

## Tecnologías utilizadas

- Java 17.
- Spring Boot 3.3.5.
- Maven.
- Spring Data JPA.
- Spring Security.
- JWT con HS256.
- BCrypt.
- Base de datos H2.
- JUnit 5.
- MockMvc.
- Git y GitHub.

## Arquitectura

El backend está compuesto por dos microservicios independientes.

| Microservicio | Puerto | Base de datos |
|---|---|---|
| usuarios-service | 8081 | usuarios-db |
| aprendizaje-service | 8082 | aprendizaje-db |

La aplicación móvil utiliza Kotlin, Jetpack Compose y MVVM.
La integración mediante Retrofit corresponde a la siguiente
etapa del proyecto.

## Microservicio de usuarios

Responsabilidades:

- Autenticación mediante RUT, contraseña y rol.
- Administración de usuarios.
- Generación de tokens JWT.
- Protección de contraseñas mediante BCrypt.
- Restricción de endpoints según el perfil.

## Microservicio de aprendizaje

Responsabilidades:

- Administración de objetivos de aprendizaje.
- Gestión de contenidos educativos.
- Aprobación y rechazo de contenidos por UTP.
- Gestión de preguntas y evaluaciones.
- Registro y corrección de respuestas.
- Control de intentos.
- Seguimiento del rendimiento.
- Generación y validación de informes pedagógicos.

## Perfiles de usuario

### Estudiante

- Consulta objetivos asignados.
- Visualiza contenidos aprobados.
- Responde desafíos y evaluaciones.
- Consulta sus propios resultados.

### Docente

- Administra objetivos, contenidos y preguntas.
- Configura los intentos de evaluación.
- Consulta resultados de estudiantes.
- Genera y valida informes pedagógicos.

### UTP

- Administra usuarios.
- Consulta el currículo.
- Aprueba o rechaza contenidos educativos.

## Usuarios ficticios de prueba

| RUT | Contraseña | Rol |
|---|---|---|
| 123456785 | 1234 | DOCENTE |
| 134567899 | 1234 | UTP |
| 111111111 | 1234 | ESTUDIANTE |
| 222222222 | 1234 | ESTUDIANTE |

Estas credenciales son exclusivamente para demostración
en un entorno académico local.

## Configuración y ejecución

1. Clonar el repositorio.
2. Abrir el proyecto en IntelliJ IDEA.
3. Importar ambos proyectos Maven.
4. Configurar la variable de entorno MPAZ_JWT_SECRET.
5. Utilizar la misma clave Base64 de al menos 32 bytes
   decodificados en ambos microservicios.
6. Ejecutar UsuariosServiceApplication.
7. Ejecutar AprendizajeServiceApplication.

La clave secreta no se almacena en GitHub.

## Direcciones de conexión

Desde el computador:

- Usuarios: http://localhost:8081
- Aprendizaje: http://localhost:8082

Desde el emulador de Android Studio:

- Usuarios: http://10.0.2.2:8081
- Aprendizaje: http://10.0.2.2:8082

HTTP se utiliza exclusivamente en el entorno local
de desarrollo y demostración.

## Datos iniciales

La aplicación incorpora material educativo de demostración:

- 4 objetivos de aprendizaje.
- 5 contenidos educativos.
- 16 preguntas.
- 4 asignaturas.
- 4 usuarios ficticios.

Las referencias curriculares deben contrastarse con
el Currículum Nacional del Ministerio de Educación.

## Pruebas automatizadas

| Microservicio | Pruebas aprobadas |
|---|---|
| usuarios-service | 12 |
| aprendizaje-service | 14 |
| Total | 26 |

Las pruebas utilizan bases de datos H2 en memoria
para evitar modificar los datos persistentes.

Se pueden ejecutar desde IntelliJ IDEA:

Maven -> Lifecycle -> clean -> test

## Seguridad

- Autenticación mediante JWT.
- Tokens con duración de una hora.
- Autorización por roles.
- Validación de firma, emisor, audiencia y vencimiento.
- Restricción de acceso a resultados de otros estudiantes.
- Informes pedagógicos exclusivos del docente.
- Consola web H2 deshabilitada.

## Contrato API

Los endpoints y las reglas de integración se encuentran
documentados en CONTRATO_API.md.

## Control de versiones

- main: rama principal.
- feature/usuarios-service: desarrollo de usuarios.
- feature/aprendizaje-service: desarrollo de aprendizaje.

Se utilizan commits descriptivos para registrar los avances.

## Estado del proyecto

Backend:
- [x] Microservicio de usuarios.
- [x] Microservicio de aprendizaje.
- [x] Bases de datos independientes.
- [x] Autenticación JWT compartida.
- [x] Endpoints REST.
- [x] Pruebas de integración del backend.

Aplicación móvil:
- [ ] Integración mediante Retrofit.
- [ ] Consumo de API externa.
- [ ] Verificación de cobertura de pruebas del 80%.
- [ ] Generación de APK firmado.

## Consideraciones

Proyecto académico desarrollado con fines educativos.
Los informes describen el rendimiento registrado y no
constituyen diagnósticos profesionales.