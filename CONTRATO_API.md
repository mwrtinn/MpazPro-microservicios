# MPAZ PRO 3.º - Contrato API v2

Evaluación Parcial 4 - DSY1105 - Duoc UC

## 1. Arquitectura

El proyecto utiliza dos microservicios independientes:

| Servicio | Puerto | URL Android (emulador) |
|---|---|---|
| usuarios-service | 8081 | http://10.0.2.2:8081/ |
| aprendizaje-service | 8082 | http://10.0.2.2:8082/ |

Cada servicio utiliza Spring Boot, Maven, JPA y su propia base H2.

La aplicación Android utiliza Kotlin, Jetpack Compose y Retrofit.

## 2. Seguridad

- Autenticación mediante RUT, contraseña y rol.
- Contraseñas protegidas mediante BCrypt.
- Tokens JWT firmados con HS256.
- Duración del token: 3600 segundos.
- Emisor: mpaz-usuarios-service.
- Audiencia: mpaz-api.
- Roles: ESTUDIANTE, DOCENTE y UTP.
- Clave secreta mediante MPAZ_JWT_SECRET.
- Peticiones protegidas: Authorization: Bearer TOKEN.
- Solo se utilizan datos ficticios.
- HTTP se limita al entorno local de desarrollo.

## 3. Usuarios-service

Estado: implementado y verificado con 12 pruebas.

| Método | Endpoint | Permiso |
|---|---|---|
| POST | /api/auth/login | Público |
| GET | /api/usuarios | DOCENTE, UTP |
| GET | /api/usuarios/{rut} | DOCENTE, UTP |
| POST | /api/usuarios | UTP |
| PUT | /api/usuarios/{rut} | UTP |
| DELETE | /api/usuarios/{rut} | UTP |

Solicitud de login:

{
"rut": "111111111",
"password": "1234",
"rol": "ESTUDIANTE"
}

Respuesta:

{
"rut": "111111111",
"nombre": "Estudiante Prueba 1",
"rol": "ESTUDIANTE",
"curso": "3° Básico",
"token": "JWT_GENERADO"
}

## 4. Aprendizaje-service

Estado: en desarrollo.
Agrupa currículo y seguimiento pedagógico.

### Objetivos de aprendizaje

| Método | Endpoint |
|---|---|
| GET | /api/objetivos |
| GET | /api/objetivos/{id} |
| POST | /api/objetivos |
| PUT | /api/objetivos/{id} |
| DELETE | /api/objetivos/{id} |

Campos:
id, asignatura, unidad, codigo, eje, descripcion,
referencia, asignado, intentosPrueba.

El docente administra los objetivos y sus intentos.
El estudiante solo consulta objetivos asignados.
UTP puede consultar los objetivos.

### Contenidos

| Método | Endpoint |
|---|---|
| GET | /api/contenidos |
| GET | /api/contenidos/{id} |
| POST | /api/contenidos |
| PUT | /api/contenidos/{id} |
| PATCH | /api/contenidos/{id}/estado |
| DELETE | /api/contenidos/{id} |

Campos:
id, oaId, titulo, explicacion, ejemplo, icono,
estado, observacionUtp, imagenPath.

Estados: PENDIENTE, APROBADO, RECHAZADO.

El docente crea y edita contenidos.
UTP aprueba o rechaza.
El estudiante solo visualiza contenidos aprobados
de objetivos asignados.

### Preguntas

| Método | Endpoint |
|---|---|
| GET | /api/preguntas |
| GET | /api/preguntas/{id} |
| POST | /api/preguntas |
| PUT | /api/preguntas/{id} |
| DELETE | /api/preguntas/{id} |

Campos:
id, oaId, tipo, dificultad, enunciado,
opcionA, opcionB, opcionC, correcta.

Tipos: DESAFIO y PRUEBA.
Dificultad: 1 a 3.
Alternativa correcta: 0 a 2.

El docente administra las preguntas.
El estudiante consulta preguntas de objetivos asignados,
sin recibir la alternativa correcta antes de responder.

### Respuestas y seguimiento

| Método | Endpoint |
|---|---|
| GET | /api/respuestas |
| GET | /api/respuestas/alumno/{rut} |
| GET | /api/intentos |
| POST | /api/respuestas |

Campos:
id, rutAlumno, oaId, preguntaId, tipo, dificultad,
intento, elegida, esCorrecta, fecha.

El servidor obtiene el RUT del estudiante desde el JWT,
valida los intentos y calcula si la respuesta es correcta.

El docente puede consultar resultados.
El estudiante solo puede consultar sus propios resultados.

### Informes pedagógicos

| Método | Endpoint |
|---|---|
| GET | /api/informes |
| GET | /api/informes/{clave} |
| PUT | /api/informes/{clave} |
| PATCH | /api/informes/{clave}/validar |

Campos:
clave, rutAlumno, oaId, rendimiento,
fortalezas, refuerzo, fecha, validado.

La clave corresponde a rutAlumno-oaId.

Los informes son exclusivos del docente,
quien puede generarlos y validarlos.
No se emiten diagnósticos automáticos.

## 5. Respuestas HTTP

- 200: Operación exitosa.
- 201: Recurso creado.
- 204: Recurso eliminado.
- 400: Datos inválidos.
- 401: Token ausente o inválido.
- 403: Permisos insuficientes.
- 404: Recurso no encontrado.
- 409: Conflicto de datos.

## 6. Integración Android

- Retrofit para ambos microservicios.
- JWT enviado mediante Authorization.
- Backend como fuente principal de datos remotos.
- Room como almacenamiento local cuando corresponda.
- Manejo de errores de conexión.
- Mantener las pantallas y arquitectura MVVM existentes.

## 7. Entrega final

- Aplicación móvil funcional.
- Microservicios con bases de datos operativas.
- Integración CRUD desde Android.
- Consumo de una API externa mediante Retrofit.
- Pruebas unitarias y cobertura mínima del 80% de la lógica.
- APK firmado en modo release.
- Documentación técnica, GitHub y Trello.