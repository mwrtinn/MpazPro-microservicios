# MPAZ PRO 3.º — Contrato API v1 (propuesta para acordar)

Evaluación Parcial 4 · DSY1105 · Duoc UC

Este contrato se basa en `Entidades.kt`, `Constantes.kt`, `AulaRepository.kt`, `Validaciones.kt`, `DatosDePrueba.kt` y los controladores/DTO existentes de usuarios-service de los ZIP entregados. Usuarios describe el código existente (sin verificación de ejecución); currículo y seguimiento son especificaciones pendientes de implementación. Los tres integrantes deben acordarlo antes de cambiar rutas o campos.

## 1. Convenciones comunes

| Servicio | Puerto | URL desde PC | URL desde emulador Android |
|---|---:|---|---|
| usuarios-service | 8081 | http://localhost:8081/ | http://10.0.2.2:8081/ |
| curriculo-service | 8082 | http://localhost:8082/ | http://10.0.2.2:8082/ |
| seguimiento-service | 8083 | http://localhost:8083/ | http://10.0.2.2:8083/ |

- Java 17, Spring Boot, Maven, JPA, H2 independiente y persistente por servicio. No compartir archivos H2.
- JSON UTF-8, nombres camelCase; rutas bajo `/api`. IDs numéricos enteros compatibles con `Int` de Kotlin. RUT normalizado (sin puntos ni guion, K mayúscula). Fechas de respuestas: epoch en milisegundos (`Long`).
- Roles: `ESTUDIANTE`, `DOCENTE`, `UTP`. Estados: `PENDIENTE`, `APROBADO`, `RECHAZADO`. Tipos de pregunta: `DESAFIO`, `PRUEBA`.
- GET de colección devuelve arreglo JSON (vacío `[]` si no hay resultados). POST de creación devuelve 201 y recurso creado; PUT/PATCH devuelve 200 y recurso actualizado; DELETE devuelve 204 sin cuerpo. POST login devuelve 200.
- Errores: 400 validación, 401 autenticación, 403 permiso insuficiente, 404 inexistente, 409 conflicto. Formato actual de usuarios: `{"error":"mensaje"}` o `{"error":"Datos inválidos","campos":{"campo":"mensaje"}}`.
- Los IDs los asigna el servicio propietario; omitir `id` al crear. No cruzar tablas de diferentes bases de datos: referenciar `oaId`, `preguntaId` y `rutAlumno`.
- Seguridad pendiente: el login actual devuelve datos del usuario, no token ni sesión autenticada. Acordar autenticación compartida y autorización por rol antes de considerar el sistema completo; no confiar en RUT/rol declarado por el cliente. HTTP local únicamente en pruebas con datos ficticios; usar HTTPS para despliegues fuera del entorno controlado. No publicar datos reales ni credenciales privadas.

## 2. Usuarios — implementado en el código entregado

| Método | Ruta | Entrada | Salida |
|---|---|---|---|
| POST | `/api/auth/login` | LoginRequest | UsuarioResponse |
| GET | `/api/usuarios?rol=ESTUDIANTE` | filtro rol opcional | UsuarioResponse[] |
| GET | `/api/usuarios/{rut}` | RUT | UsuarioResponse |
| POST | `/api/usuarios` | UsuarioRequest | UsuarioResponse |
| PUT | `/api/usuarios/{rut}` | UsuarioUpdateRequest | UsuarioResponse |
| DELETE | `/api/usuarios/{rut}` | RUT | sin cuerpo |

LoginRequest de prueba (usuario ficticio):
```json
{"rut":"111111111","password":"1234","rol":"ESTUDIANTE"}
```
UsuarioResponse (ejemplo):
```json
{"rut":"111111111","nombre":"Estudiante Prueba 1","rol":"ESTUDIANTE","curso":"3° Básico"}
```
UsuarioRequest: `rut`, `nombre`, `password`, `rol`, `curso` (opcional). UsuarioUpdateRequest: `nombre`, `rol`, `curso`, `password` opcional (si se omite se conserva). Nombre 3–80 caracteres, contraseña 4–64, RUT válido para creación. Nunca incluir hash ni contraseña en respuestas. Android no debe enviar SHA-256 al backend: BCrypt valida la contraseña original. No guardar contraseña ni hash en el DTO remoto.

## 3. Currículo — compañero 1 (pendiente)

### Modelos JSON de respuesta

Objetivo (ejemplo basado en datos de prueba; códigos OA por verificar):
```json
{"id":1,"asignatura":"Matemática","unidad":"Unidad 1: Números hasta el 1.000","codigo":"MA03 OA 01","eje":"Números y operaciones","descripcion":"Contar números del 0 al 1.000.","referencia":"Currículum Nacional (por verificar)","asignado":true,"intentosPrueba":2}
```
Contenido:
```json
{"id":1,"oaId":1,"titulo":"Contar de 5 en 5 y de 10 en 10","explicacion":"Para contar de cinco en cinco, sumamos cinco cada vez.","ejemplo":"5, 10, 15, 20","icono":"🔢","estado":"APROBADO","observacionUtp":"","imagenPath":null}
```
Pregunta (vista completa para docente y uso interno autorizado):
```json
{"id":1,"oaId":1,"tipo":"DESAFIO","dificultad":1,"enunciado":"Cuenta de 2 en 2: 2, 4, 6, 8, ¿qué número sigue?","opcionA":"9","opcionB":"10","opcionC":"12","correcta":1}
```

### Endpoints propuestos

| Método | Ruta | Operación |
|---|---|---|
| GET | `/api/objetivos` | Listar; filtro `asignado=true` opcional |
| GET | `/api/objetivos/{id}` | Consultar objetivo |
| POST | `/api/objetivos` | Crear objetivo |
| PUT | `/api/objetivos/{id}` | Actualizar objetivo completo sin cambiar ID |
| DELETE | `/api/objetivos/{id}` | Eliminar si no hay dependencias |
| GET | `/api/contenidos` | Listar; filtros `oaId`, `estado` opcionales |
| GET | `/api/contenidos/{id}` | Consultar contenido |
| POST | `/api/contenidos` | Crear en PENDIENTE |
| PUT | `/api/contenidos/{id}` | Editar contenido; si cambia material pedagógico, vuelve a PENDIENTE |
| PATCH | `/api/contenidos/{id}/estado` | Revisar por UTP |
| DELETE | `/api/contenidos/{id}` | Eliminar contenido si procede |
| GET | `/api/preguntas` | Listar; filtros `oaId`, `tipo` opcionales |
| GET | `/api/preguntas/{id}` | Consultar pregunta |
| POST | `/api/preguntas` | Crear pregunta |
| PUT | `/api/preguntas/{id}` | Actualizar pregunta |
| DELETE | `/api/preguntas/{id}` | Eliminar si no hay respuestas asociadas |

POST/PUT usan los campos del modelo salvo `id`; el servidor controla estado y observación UTP. PATCH de revisión: `{"estado":"RECHAZADO","observacionUtp":"Corregir el ejemplo."}`; para aprobar: `{"estado":"APROBADO","observacionUtp":""}`.

Reglas: intentosPrueba 1–3; dificultad 1–3; alternativas y correcta 0=A, 1=B, 2=C. Título 3–60, explicación 20–400, ejemplo 5–200, enunciado 10–250, alternativas 1–60 y distintas (sin distinguir mayúsculas); rechazo exige observación 5–200. Validar existencia del OA. Orden de preguntas: dificultad, ID. Estudiante solo consulta objetivos asignados y contenidos aprobados. No borrar o editar preguntas ya respondidas de forma que se invalide historial. Para el MVP reutilizar `DatosDePrueba.kt` con 4 OA, 5 contenidos y 16 preguntas, sembrando una sola vez y respetando relaciones; OA oficiales y autorización pedagógica deben verificarse. No enviar rutas privadas del dispositivo en `imagenPath`; mantener null o acordar una URL de recurso autorizado servida por backend. En la versión definitiva, ocultar `correcta` al estudiante antes de contestar (adaptación coordinada del quiz; no cambiar el modelo unilateralmente).

## 4. Seguimiento — compañero 2 (pendiente)

Respuesta (modelo actual, salida propuesta):
```json
{"id":1,"rutAlumno":"111111111","oaId":1,"preguntaId":1,"tipo":"DESAFIO","dificultad":1,"intento":1,"elegida":1,"esCorrecta":true,"fecha":1795478400000}
```
Informe:
```json
{"clave":"111111111-1","rutAlumno":"111111111","oaId":1,"rendimiento":100,"fortalezas":"Preguntas de nivel 1.","refuerzo":"Sin contenidos que requieran refuerzo por ahora.","fecha":"24/11/2026 10:00","validado":false}
```

| Método | Ruta | Operación |
|---|---|---|
| GET | `/api/respuestas` | Listar respuestas (docente) |
| GET | `/api/respuestas/alumno/{rut}` | Consultar progreso del estudiante; filtro `oaId` opcional |
| GET | `/api/intentos?rutAlumno=111111111&oaId=1&tipo=PRUEBA` | Último intento registrado; respuesta `{"ultimoIntento":0}` si ninguno |
| POST | `/api/respuestas` | Guardar respuesta y devolver registro confirmado |
| GET | `/api/informes` | Listar informes (docente); filtros `rutAlumno`, `oaId` opcionales |
| GET | `/api/informes/{clave}` | Consultar informe (docente) |
| PUT | `/api/informes/{clave}` | Crear o regenerar informe (201 si nuevo, 200 si existente) |
| PATCH | `/api/informes/{clave}/validar` | Validación docente, cuerpo `{"validado":true}` |

POST de respuesta: en el prototipo el cliente ya tiene los campos del modelo; como objetivo de integración segura, enviar solo `oaId`, `preguntaId`, `intento`, `elegida` (y `rutAlumno` solo como referencia cotejada con identidad autenticada). El servidor consulta currículo para determinar tipo, dificultad, respuesta correcta, fecha y límite de intentos. Acordar la transición con el responsable de currículo y del login antes de integrarla. El servidor comprueba identidad, relación OA/pregunta, intento permitido y alternativa 0–2. Evitar duplicados por estudiante/pregunta/tipo/intento, incluso si se reintenta una petición. No avanzar el quiz hasta confirmar el guardado remoto; mostrar error y permitir reintentar. Conservar las respuestas como historial, sin CRUD destructivo de parte del estudiante. El límite de intentos debe comprobarse también en el servidor y permitir las distintas preguntas del mismo intento; definir inicio/cierre de intento para evitar reabrir intentos finalizados.

Informe: clave `rutAlumno-oaId`, rendimiento entero 0–100; fecha `dd/MM/yyyy HH:mm` según modelo actual. PUT recibe campos del informe excepto clave (derivada de la ruta); al regenerar vuelve a `validado=false`. Reutilizar lógica de `GeneradorInforme.kt`: último intento de prueba o práctica si no hay prueba; fortalezas por dificultad >=70%, refuerzo <50%. Solo docente consulta, genera, descarga y valida. No emitir diagnósticos ni decisiones académicas. Preferir cálculo a partir de respuestas persistidas y no confiar en porcentajes enviados por el cliente.

## 5. Integración Android — acuerdos antes de programar

- Mantener pantallas Compose y firmas existentes del repositorio cuando sea viable. El coordinador modifica `build.gradle.kts`, `libs.versions.toml`, configuración de red, `ViewModelFactory.kt` y define inyección/estructura común.
- Separar `data/remote/usuarios`, `data/remote/curriculo` y `data/remote/seguimiento` con DTO y API por módulo. Cada integrante modifica solo sus archivos; el coordinador integra cambios compartidos de `AulaRepository.kt` y ViewModels que cruzan módulos.
- Mantener Room temporalmente para módulos no migrados. Una vez conectado un módulo, el backend es fuente de verdad; si hay caché, actualizarla solo tras respuesta válida y preservar IDs de servidor. No mezclar semillas locales y remotas en el mismo flujo. Mantener los Flow que observan las pantallas, refrescándolos después de las operaciones remotas. Manejar errores y carga sin cerrar la app.
- Revisar `sembrarSiVacio()` al migrar: actualmente depende del conteo de usuarios. Separar semillas por módulo antes de sustituir el login para no dejar currículo sin datos.
- Toda modificación de rutas, modelos, reglas o seguridad requiere actualizar este contrato en PR y comunicarla al equipo.

## 6. Criterio de entrega de cada módulo

1. Arranca en su puerto y persiste datos al reiniciar.
2. Pruebas de endpoints válidos, inválidos y permisos; `mvn test` exitoso.
3. Ejemplos de peticiones y respuestas en README; datos ficticios.
4. PR revisado y commits técnicos propios. Integración Android demuestra lectura, escritura y actualización según perfil; no solo Postman.
5. Pruebas unitarias de ViewModel/Repository y evidencia de cobertura global de lógica >=80% (rúbrica). API externa por Retrofit, APK firmado y Trello quedan en el plan general.
