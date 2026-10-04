# ExpresoFast — Laboratorio 11: Formularios Reactivos Avanzados y Consolidación Full-Stack

| | |
|---|---|
| **Estudiante** | Debora Zavala Nuñez |
| **Carné** | C5L189 |
| **Curso** | IF0009 - Desarrollo de Software IV |
| **Profesor** | Mag. Jonathan Granados C. |
| **Institución** | Universidad de Costa Rica, Sede del Atlántico, Recinto Paraíso |
| **Semestre** | II-2026 |

---

## Tabla de contenido

1. [Descripción general](#1-descripción-general)
2. [Estructura del repositorio](#2-estructura-del-repositorio)
3. [Stack tecnológico](#3-stack-tecnológico)
4. [Base de datos (SQL Server)](#4-base-de-datos-sql-server)
5. [Back-End (Spring Boot 3)](#5-back-end-spring-boot-3)
6. [Front-End (Angular 19)](#6-front-end-angular-19)
7. [Ejecución del proyecto](#7-ejecución-del-proyecto)
8. [Guía rápida de pruebas manuales](#8-guía-rápida-de-pruebas-manuales)
9. [Fundamentación teórica](#9-fundamentación-teórica)

---

## 1. Descripción general

Un mismo número de envío (tracking) puede contener **múltiples paquetes** de distintos pesos y descripciones. Este laboratorio extiende ExpresoFast para registrar un envío completo con todos sus paquetes en una sola operación, con:

- Relación **1:N** `Envio` → `PAQUETES` persistida de forma transaccional en SQL Server.
- Formulario **reactivo y estrictamente tipado** con un `FormArray` dinámico.
- **Validación cruzada síncrona** de fechas a nivel de `FormGroup`.
- **Validación asíncrona** del número de tracking contra el API.

---

## 2. Estructura del repositorio

```
.
├── README.md
├── database/
│   └── lab11_paquetes.sql
├── expresofast-backend/
│   └── src/main/java/com/expresofast/
│       ├── controller/   EnvioController
│       ├── service/      EnvioService, EnvioServiceImpl
│       ├── repository/   EnvioRepository
│       ├── model/        Envio, Paquete, EstadoEnvio
│       ├── dto/          EnvioRegistroDTO, PaqueteDTO, EnvioDTO,
│       │                 TrackingExisteDTO, ActualizarEstadoDTO
│       ├── exception/    GlobalExceptionHandler, ResourceNotFoundException,
│       │                 TrackingDuplicadoException
│       └── config/       WebConfig (CORS)
└── expresofast-frontend/
    └── src/app/
        ├── components/
        │   ├── envio-avanzado-form/   Formulario reactivo (Lab 11)
        │   ├── envio-list/            Dashboard de envíos
        │   └── envio-tracking/        Rastreo por código
        ├── validators/
        │   ├── fechas.validator.ts
        │   └── tracking-unico.validator.ts
        ├── services/envio.service.ts
        └── models/envio.model.ts
```

---

## 3. Stack tecnológico

| Capa | Tecnología |
|---|---|
| Base de datos | SQL Server |
| Back-End | Java 21, Spring Boot 3.2.5, Spring Data JPA (Hibernate 6), Bean Validation |
| Front-End | Angular 19 Standalone, Reactive Forms tipados, RxJS 7 |

---

## 4. Base de datos (SQL Server)

El script oficial del laboratorio se encuentra en [`database/lab11_paquetes.sql`](database/lab11_paquetes.sql). Debe ejecutarse sobre la base `ExpresoFastc5l189_II2026`.

### 4.1 Tabla `PAQUETES` (relación 1:N)

```sql
CREATE TABLE PAQUETES (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    envio_id BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    peso_kg DECIMAL(5,2) NOT NULL,
    CONSTRAINT FK_Paquetes_Envios FOREIGN KEY (envio_id)
        REFERENCES Envio(envio_id) ON DELETE CASCADE
);
```

> El enunciado referencia `ENVIOS(id)`. En la base de datos de ExpresoFast la tabla se llama `Envio` y su llave primaria es `envio_id` (ver diagrama de la base), por lo que la llave foránea apunta a `Envio(envio_id)`.

### 4.2 Ajustes adicionales sobre `Envio`

El script también:

- Agrega las columnas `fecha_despacho` y `fecha_entrega_estimada` (tipo `DATE`, nulables) para persistir las dos fechas operativas que captura el formulario.
- Garantiza que `vehiculo_id` y `conductor_id` acepten `NULL`, ya que el registro de un envío no asigna vehículo ni conductor en esta etapa.

Todas las instrucciones están protegidas con verificaciones de existencia, por lo que el script se puede ejecutar más de una vez sin errores.

---

## 5. Back-End (Spring Boot 3)

### 5.1 Mapeo JPA

| Entidad | Tabla | Relación |
|---|---|---|
| `Envio` | `Envio` | `@OneToMany(mappedBy = "envio", cascade = ALL, orphanRemoval = true)` hacia `Paquete` |
| `Paquete` | `PAQUETES` | `@ManyToOne(fetch = LAZY)` hacia `Envio` mediante `envio_id` |

La relación es **bidireccional**. `Envio.agregarPaquete()` mantiene sincronizados ambos lados. El peso total del envío (`peso_kg`) se calcula en el servicio como la suma de los pesos de sus paquetes.

### 5.2 DTOs

- **`EnvioRegistroDTO`**: recibe el `POST` desde Angular con `numeroTracking`, `destinatario`, `direccionDestino`, `montoFlete`, `fechaDespacho`, `fechaEntregaEstimada` y `List<PaqueteDTO> paquetes` (mínimo 1, validados en cascada con `@Valid`).
- **`PaqueteDTO`**: `descripcion` (obligatoria, máx. 255) y `pesoKg` (entre 0.01 y 999.99, máximo 2 decimales).
- **`EnvioDTO`**: respuesta con los datos del envío, `pesoTotalKg` y la lista de paquetes.
- **`TrackingExisteDTO`**: respuesta del endpoint de validación (`trackingNumber`, `existe`).

### 5.3 Transaccionalidad

`EnvioServiceImpl.registrarEnvio()` está anotado con `@Transactional`. El envío y todos sus paquetes se insertan en una única transacción: si falla la inserción de cualquier paquete, se revierte todo (*rollback*), sin dejar envíos huérfanos. Las consultas de lectura usan `@Transactional(readOnly = true)` y un `@EntityGraph` para cargar los paquetes en una sola consulta (evita el problema N+1).

### 5.4 Reglas de negocio aplicadas también en el servidor

El Front-End valida, pero el servidor **nunca confía** en el cliente:

- La fecha de entrega estimada debe ser estrictamente posterior a la de despacho (`400 Bad Request`).
- El número de tracking no puede repetirse (`409 Conflict`), incluso si dos usuarios intentan registrarlo al mismo tiempo.

### 5.5 Endpoints

La ruta base es `/api/v1/envios` y también responde bajo `/api/envios` (ruta indicada en el enunciado).

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/envios/check-tracking/{trackingNumber}` | Indica si el número de rastreo ya existe. Responde `{"trackingNumber":"...","existe":true}` |
| `POST` | `/api/v1/envios` | Registra un envío con su lista de paquetes (`201 Created`) |
| `GET` | `/api/v1/envios` | Lista todos los envíos con sus paquetes |
| `GET` | `/api/v1/envios/rastreo/{codigo}` | Detalle de un envío por código de rastreo |
| `PATCH` | `/api/v1/envios/{id}/estado` | Actualiza el estado del envío |

Ejemplo de cuerpo para `POST /api/v1/envios`:

```json
{
  "numeroTracking": "EXP-2026-5001",
  "destinatario": "Carlos Vindas Mora",
  "direccionDestino": "Cartago, Paraíso, Centro",
  "montoFlete": 8500.00,
  "fechaDespacho": "2026-10-05",
  "fechaEntregaEstimada": "2026-10-08",
  "paquetes": [
    { "descripcion": "Caja de repuestos", "pesoKg": 2.50 },
    { "descripcion": "Sobre de documentos", "pesoKg": 0.75 }
  ]
}
```

---

## 6. Front-End (Angular 19)

El componente `EnvioAvanzadoFormComponent` (ruta `/nuevo-envio`) reemplaza al formulario basado en plantillas del laboratorio anterior. **No se utiliza `[(ngModel)]` para capturar los datos del envío.**

### 6.1 Formularios estrictamente tipados

Se usa `NonNullableFormBuilder` con interfaces que describen el modelo del formulario:

```ts
interface PaqueteForm {
  descripcion: FormControl<string>;
  pesoKg: FormControl<number | null>;
}

interface EnvioAvanzadoForm {
  numeroTracking: FormControl<string>;
  destinatario: FormControl<string>;
  direccionDestino: FormControl<string>;
  montoFlete: FormControl<number | null>;
  fechaDespacho: FormControl<string>;
  fechaEntregaEstimada: FormControl<string>;
  paquetes: FormArray<FormGroup<PaqueteForm>>;
}
```

TypeScript rechaza en compilación asignar, por ejemplo, un `string` a `pesoKg`.

### 6.2 Arreglo dinámico (`FormArray`)

- **"+ Añadir Paquete"** inserta un nuevo `FormGroup` (`descripcion`, `pesoKg`) con `push()`.
- **"X"** elimina el paquete con `removeAt(i)`. El botón se deshabilita y el método lo vuelve a verificar, de modo que **siempre quede al menos 1 paquete**.
- Se muestra el **peso total** calculado en tiempo real a partir del arreglo.

### 6.3 Validación cruzada síncrona (`validators/fechas.validator.ts`)

Un `ValidatorFn` aplicado al **`FormGroup`** completo compara ambas fechas como instantes de tiempo y retorna `{ fechasInvalidas: true }` si `fechaEntregaEstimada` no es **estrictamente mayor** que `fechaDespacho`.

Cuando la regla se incumple:
- Se muestra un **mensaje de error global** en la interfaz.
- El botón de **Submit queda deshabilitado** (`form.invalid`).

### 6.4 Validación asíncrona (`validators/tracking-unico.validator.ts`)

Un `AsyncValidatorFn` aplicado al control `numeroTracking`:

1. Espera 400 ms desde la última pulsación (*debounce* con `timer`) para no saturar el API.
2. Consulta `GET /api/v1/envios/check-tracking/{trackingNumber}`.
3. Si el número existe, marca el control como inválido con `{ trackingTomado: true }` y muestra: **"Este número de rastreo ya está en uso"**.
4. Mientras la consulta está en curso el control queda en estado `PENDING` (el botón de envío permanece deshabilitado) y se muestra "Verificando disponibilidad...".

Si el usuario sigue escribiendo, Angular cancela la suscripción anterior, por lo que nunca se evalúa una respuesta obsoleta. Como respaldo, si el servidor responde `409`, el error `trackingTomado` se enlaza al control igualmente.

---

## 7. Ejecución del proyecto

### Requisitos

- Java 21 y Maven 3.8+
- Node.js 18+ y Angular CLI 19
- Acceso a la base de datos SQL Server configurada en `application.properties`

### Paso 1 — Base de datos

Ejecute [`database/lab11_paquetes.sql`](database/lab11_paquetes.sql) en SQL Server Management Studio sobre la base `ExpresoFastc5l189_II2026`.

### Paso 2 — Back-End

```bash
cd expresofast-backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. La documentación Swagger está en `http://localhost:8080/swagger-ui.html`.

Pruebas unitarias:

```bash
mvn test
```

### Paso 3 — Front-End

```bash
cd expresofast-frontend
npm install
ng serve
```

La aplicación queda disponible en `http://localhost:4200`.

---

## 8. Guía rápida de pruebas manuales

| # | Escenario | Resultado esperado |
|---|---|---|
| 1 | Abrir `/nuevo-envio` | Se muestra 1 paquete; el botón "X" está deshabilitado |
| 2 | Presionar "+ Añadir Paquete" varias veces | Aparecen bloques nuevos y el peso total se actualiza |
| 3 | Eliminar paquetes hasta quedar 1 | No permite eliminar el último |
| 4 | Fecha de entrega igual o anterior al despacho | Mensaje de error global y Submit deshabilitado |
| 5 | Digitar un tracking existente (p. ej. uno ya registrado) | "Este número de rastreo ya está en uso" |
| 6 | Digitar un tracking nuevo | "Número de rastreo disponible" |
| 7 | Completar y enviar el formulario | Se guarda el envío y sus paquetes; el dashboard muestra cantidad de paquetes y peso total |
| 8 | Revisar en SQL Server `SELECT * FROM PAQUETES` | Los paquetes quedan asociados al `envio_id` creado |

---

## 9. Fundamentación teórica

### 9.1 UX y escalabilidad: `FormArray` frente a 10 campos estáticos ocultos

**Modelo de datos, no de DOM.** Un `FormArray` es una colección *viva* de controles cuya longitud se define en tiempo de ejecución. El formulario refleja exactamente la cardinalidad real del dominio (1 envío tiene N paquetes) en lugar de imponer un máximo arbitrario codificado en el HTML. Con 10 campos estáticos ocultos, el envío número 11 es imposible sin modificar el código, y un envío de 2 paquetes arrastra 8 bloques inertes.

**Experiencia de usuario (UX).**

- El operador **ve únicamente lo que necesita**. Aparecen bloques bajo demanda, lo que reduce la carga cognitiva y el desplazamiento vertical en pantalla.
- La retroalimentación es **inmediata y localizada**. Cada paquete tiene su propio estado (`touched`, `dirty`, `invalid`), por lo que el error se muestra junto al campo exacto que lo causa.
- Los datos derivados, como el **peso total**, se recalculan al instante porque el `FormArray` expone `value` y `valueChanges` agregados.
- Los campos ocultos con `display: none` siguen existiendo en el DOM: pueden validarse (bloqueando el envío por un error invisible para el usuario), confundir a los lectores de pantalla y ser enviados con valores vacíos. Un `FormArray` solo contiene controles que existen y se renderizan.

**Mantenibilidad.**

| Aspecto | 10 campos estáticos | `FormArray` |
|---|---|---|
| Agregar un campo (p. ej. `alto_cm`) | Editar 10 bloques de HTML y 10 definiciones en TypeScript | Una línea en la fábrica `crearPaquete()` y una en la plantilla |
| Validaciones | Duplicadas 10 veces | Definidas una sola vez y reutilizadas por cada elemento |
| Estado global | Se debe recorrer cada campo manualmente | `form.valid`, `form.value` y `form.pending` ya agregan todo |
| Límite de elementos | Fijo (10) | Ilimitado |
| Pruebas | Dependen del DOM | Se prueban sobre el modelo, sin renderizar |
| Tipado | Nombres frágiles (`paquete1_peso`, `paquete2_peso`...) | `FormArray<FormGroup<PaqueteForm>>` verificado por el compilador |

La plantilla utiliza un único bloque `@for` sobre `paquetes.controls`, aplicando el principio **DRY**: la lógica y la presentación de un paquete existen una sola vez, sin importar cuántos se creen.

### 9.2 Ciclo de eventos: validador síncrono frente a validador asíncrono

JavaScript ejecuta el código en un **único hilo**, coordinado por el *Event Loop*, que toma tareas de las colas únicamente cuando la **pila de llamadas (*call stack*)** está vacía. Las colas de microtareas (*promesas*) tienen prioridad sobre la cola de macrotareas (*timers*, eventos de E/S, red).

**Validador cruzado de fechas (síncrono).**
Se ejecuta íntegramente **dentro de la pila de llamadas**, durante la propia llamada a `updateValueAndValidity()` que Angular dispara cuando el usuario modifica un campo. Calcula ambas fechas, compara y retorna el resultado (`ValidationErrors | null`) de inmediato; el estado del formulario queda actualizado antes de que la pila se libere y el navegador repinte. Es apropiado porque la operación es puramente computacional, instantánea y no depende de recursos externos.

**Validador de tracking (asíncrono).**
Necesita un recurso externo (la base de datos, vía HTTP) cuya latencia es impredecible. En lugar de bloquear el hilo, ocurre lo siguiente:

1. El validador se ejecuta, **programa** el trabajo (`timer(400)` y la petición HTTP) y **retorna inmediatamente** un `Observable`. La pila se libera.
2. El temporizador y la petición de red son gestionados por las **Web APIs del navegador**, fuera del hilo principal. Mientras tanto, el control queda en estado **`PENDING`** y la interfaz sigue respondiendo.
3. Cuando el temporizador vence o llega la respuesta, el callback correspondiente entra en la cola de **macrotareas**. El Event Loop lo ejecuta cuando la pila está vacía.
4. El `Observable` emite, Angular recibe el resultado, asigna `trackingTomado: true` o `null` al control y recalcula su estado (`VALID` / `INVALID`).

**¿Por qué Angular exige retornar un `Observable` o `Promise`?**

- **No se puede esperar de forma bloqueante en JavaScript.** Esperar sincrónicamente la respuesta del servidor congelaría el único hilo: la interfaz no respondería, no se repintaría y el usuario no podría escribir. El `Observable` o la `Promise` representan un **valor futuro** que se entrega cuando esté disponible, sin detener el hilo.
- **Permite el estado `PENDING`.** Angular se suscribe al resultado y, mientras no emita, mantiene al control en espera, lo cual evita que el formulario se envíe antes de conocer si el tracking es único.
- **El `Observable` además es cancelable.** Si el usuario escribe otro carácter, Angular se desuscribe de la validación anterior; combinado con el *debounce* se evitan peticiones innecesarias y condiciones de carrera (*race conditions*) en las que una respuesta antigua sobrescriba a una más reciente. Una `Promise` no se puede cancelar.
- **Orden de ejecución.** Angular ejecuta los validadores asíncronos únicamente si todos los síncronos del control ya pasaron; así no se hacen consultas al API para valores que ya son inválidos (por ejemplo, un tracking vacío o con caracteres no permitidos).

En resumen: el validador síncrono *calcula* dentro de la pila y responde de inmediato, mientras que el asíncrono *delega* el trabajo al entorno, devuelve una promesa de resultado y permite que el Event Loop continúe atendiendo la interfaz hasta que la respuesta llegue.
