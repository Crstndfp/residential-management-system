# QA Test Plan & Acceptance Matrix: Épica 5 — Tickets de Mantenimiento e Incidencias (US-501 & US-502)

Este documento proporciona a los ingenieros de QA y testers la especificación técnica completa, matriz de aceptación, guía paso a paso con comandos cURL y verificación en base de datos para validar las historias de usuario **US-501** y **US-502**.

---

## 1. Alcance y Criterios de Aceptación (Gherkin)

### US-501: Levantamiento de Ticket de Incidencia con Fotografías
* **Como:** Residente (Propietario o Inquilino desde Web o App Android).
* **Quiero:** Reportar un desperfecto o necesidad de mantenimiento en áreas comunes seleccionando categoría, ubicación y adjuntando fotografía/evidencia.
* **Para:** Que la administración del residencial atienda oportunamente los problemas físicos de la comunidad.
* **Criterios de Aceptación:**
  - **Given** que un residente autenticado detecta un desperfecto (ej. lámpara rota, bomba de piscina averiada).
  - **When** llena el formulario de reporte con título, categoría (`LIGHTING`, `GARDENING`, `POOL`, `SECURITY_EQUIPMENT`, `PLUMBING`, `OTHER`), ubicación, descripción y URL de evidencia fotográfica.
  - **Then** el sistema persiste el ticket con un folio único e irrepetible (formato `TCK-XXXXXXXX`) y estado inicial `OPEN`.
  - **And** se registra la entrada inicial de auditoría en la línea de tiempo (`ticket_updates`).
  - **And** queda indexado para consulta tanto por el residente como en el panel de la Junta Directiva y Administrador Operativo.

### US-502: Seguimiento de Avance y Notificaciones Push al Residente
* **Como:** Administrador Operativo o Vocal de la Junta Directiva (desde la Web).
* **Quiero:** Actualizar el estado del ticket (`IN_PROGRESS`, `RESOLVED`, `CLOSED`), asignar técnico responsable y añadir notas de avance.
* **Para:** Mantener informada a la comunidad y asegurar la trazabilidad del mantenimiento preventivo y correctivo.
* **Criterios de Aceptación:**
  - **Given** que un administrador accede a un ticket en estado `OPEN`.
  - **When** asigna un técnico y actualiza el estado a `IN_PROGRESS` con notas explicativas.
  - **Then** el sistema guarda el cambio de estado, actualiza la fecha `updated_at` y persiste la bitácora histórica en `amenities_schema.ticket_updates`.
  - **And** el sistema despacha una notificación push clasificada al residente reportante informando el avance.
  - **When** el técnico concluye los trabajos y se marca como `RESOLVED` o `CLOSED`, se estampa la marca de tiempo `closed_at`.
  - **And** si se intenta modificar el estado de un ticket ya en estado `CLOSED`, el sistema bloquea la operación con `400 Bad Request` (`INVALID_TICKET_STATUS_TRANSITION`).

---

## 2. Matriz de Pruebas y Casos de Aceptación

| Caso ID | Historia | Escenario / Objetivo | Método / Endpoint | Payload de Entrada | Código Esperado | Estado Esperado en BD / Sistema |
| :--- | :---: | :--- | :---: | :--- | :---: | :--- |
| **TC-501-01** | US-501 | Levantamiento exitoso de ticket por residente | `POST /api/v1/amenities/tickets` | `title: "Lámpara quebrada"`, `category: "LIGHTING"`, `location: "Calle 3"`, `photoUrl: "https://..."` | **201 Created** | Ticket creado con folio `TCK-...`, estado `OPEN` y registro inicial en `ticket_updates`. |
| **TC-501-02** | US-501 | Invariante: Rechazar ticket sin campos obligatorios | `POST /api/v1/amenities/tickets` | `title: ""`, `category: null`, `location: ""` | **400 Bad Request** | Error de validación Bean Validation con detalles de campos requeridos. |
| **TC-501-03** | US-501 | Listar tickets con filtro por estado | `GET /api/v1/amenities/tickets?status=OPEN` | Query param `status=OPEN` | **200 OK** | Colección de tickets en estado `OPEN`. |
| **TC-501-04** | US-501 | Consulta de detalle con historial de bitácora | `GET /api/v1/amenities/tickets/{id}` | ID de ticket existente | **200 OK** | Objeto `ticket` y array `history` cronológico ordenado ascendentemente. |
| **TC-502-01** | US-502 | Transición exitosa a IN_PROGRESS con asignación de técnico | `PATCH /api/v1/amenities/tickets/{id}/progress` | `newStatus: "IN_PROGRESS"`, `assignedTechnician: "Cuadrilla 2"`, `notes: "Revisión programada"` | **200 OK** | Estado `IN_PROGRESS`, técnico asignado, bitácora registrada y push despachado. |
| **TC-502-02** | US-502 | Transición exitosa a RESOLVED / CLOSED | `PATCH /api/v1/amenities/tickets/{id}/progress` | `newStatus: "RESOLVED"`, `notes: "Foco sustituido y cableado reparado"` | **200 OK** | Estado `RESOLVED`, `closed_at` no nulo, bitácora actualizada. |
| **TC-502-03** | US-502 | Invariante: Bloqueo de modificación a ticket cerrado | `PATCH /api/v1/amenities/tickets/{id}/progress` | `newStatus: "IN_PROGRESS"` sobre ticket en `CLOSED` | **400 Bad Request** | Error `INVALID_TICKET_STATUS_TRANSITION`. No se permite reabrir ticket cerrado. |
| **TC-502-04** | US-502 | Invariante: Error 404 al actualizar ticket inexistente | `PATCH /api/v1/amenities/tickets/{id}/progress` | UUID aleatorio inexistente | **404 Not Found** | Error `TICKET_NOT_FOUND`. |

---

## 3. Guía Paso a Paso para Testers (Ejecución Manual / Postman / cURL)

### Requisitos Previos:
1. Microservicio `amenities-service` ejecutándose en el puerto `8083`.
2. Base de datos PostgreSQL local corriendo con esquema `amenities_schema` y migración `V8` aplicada.

---

### Paso 1: Levantar un Nuevo Ticket de Incidencia (US-501)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Foco de alumbrado publico fundido",
    "category": "LIGHTING",
    "location": "Poste frente a la Casa B-105",
    "description": "La luminaria parpadea constantemente y se apaga en la noche.",
    "photoUrl": "https://storage.local/evidencias/foco-b105.jpg",
    "priority": "HIGH",
    "reporterUserId": "11111111-1111-1111-1111-111111111111",
    "reporterName": "Ana Morales",
    "reporterRole": "OWNER",
    "houseId": "22222222-2222-2222-2222-222222222222",
    "houseNumber": "B-105"
  }'
```
* **Respuesta esperada:** `201 Created`
```json
{
  "id": "...",
  "folio": "TCK-XXXXXXXX",
  "title": "Foco de alumbrado publico fundido",
  "category": "LIGHTING",
  "location": "Poste frente a la Casa B-105",
  "description": "La luminaria parpadea constantemente y se apaga en la noche.",
  "photoUrl": "https://storage.local/evidencias/foco-b105.jpg",
  "status": "OPEN",
  "priority": "HIGH",
  "assignedTechnician": null,
  "reporterUserId": "11111111-1111-1111-1111-111111111111",
  "reporterName": "Ana Morales",
  "reporterRole": "OWNER",
  "houseId": "22222222-2222-2222-2222-222222222222",
  "houseNumber": "B-105",
  "createdAt": "...",
  "updatedAt": "...",
  "closedAt": null
}
```

---

### Paso 2: Consultar la Lista de Tickets Abiertos (US-501)
```bash
curl -i -X GET "http://localhost:8083/api/v1/amenities/tickets?status=OPEN"
```
* **Respuesta esperada:** `200 OK` con un arreglo JSON de tickets abiertos.

---

### Paso 3: Asignar Técnico y Pasar a EN PROCESO (US-502)
```bash
TICKET_ID="<ID_DEL_TICKET_DEL_PASO_1>"

curl -i -X PATCH "http://localhost:8083/api/v1/amenities/tickets/${TICKET_ID}/progress" \
  -H "Content-Type: application/json" \
  -d '{
    "authorUserId": "33333333-3333-3333-3333-333333333333",
    "authorName": "Ing. Roberto Diaz (Administrador)",
    "authorRole": "ADMIN",
    "newStatus": "IN_PROGRESS",
    "assignedTechnician": "Tecnico Electricista Juan Perez",
    "notes": "Se asigna orden de trabajo al electricista para reemplazo de luminaria LED."
  }'
```
* **Respuesta esperada:** `200 OK`
```json
{
  "ticket": {
    "id": "...",
    "folio": "TCK-XXXXXXXX",
    "status": "IN_PROGRESS",
    "assignedTechnician": "Tecnico Electricista Juan Perez",
    ...
  },
  "history": [
    {
      "previousStatus": "OPEN",
      "newStatus": "OPEN",
      "notes": "Ticket opened with initial status OPEN",
      ...
    },
    {
      "authorName": "Ing. Roberto Diaz (Administrador)",
      "previousStatus": "OPEN",
      "newStatus": "IN_PROGRESS",
      "notes": "Se asigna orden de trabajo al electricista para reemplazo de luminaria LED.",
      ...
    }
  ]
}
```
* **Verificación de logs de backend:**  
  `[PUSH-DISPATCH] Dispatched push notification to resident [11111111-1111-1111-1111-111111111111]: Ticket [TCK-XXXXXXXX] changed to [IN_PROGRESS] - Details: Se asigna orden de trabajo al electricista para reemplazo de luminaria LED.`

---

### Paso 4: Marcar como Resuelto y Cerrar el Ticket (US-502)
```bash
curl -i -X PATCH "http://localhost:8083/api/v1/amenities/tickets/${TICKET_ID}/progress" \
  -H "Content-Type: application/json" \
  -d '{
    "authorUserId": "33333333-3333-3333-3333-333333333333",
    "authorName": "Ing. Roberto Diaz (Administrador)",
    "authorRole": "ADMIN",
    "newStatus": "RESOLVED",
    "assignedTechnician": "Tecnico Electricista Juan Perez",
    "notes": "Luminaria sustituida con exito. Circuito probado y funcionando al 100%."
  }'
```
* **Respuesta esperada:** `200 OK` con estado `"RESOLVED"`, `closedAt` registrado y bitácora completa.

---

### Paso 5: Consultar Detalle Completo de la Incidencia (US-501 / US-502)
```bash
curl -i -X GET "http://localhost:8083/api/v1/amenities/tickets/${TICKET_ID}"
```
* **Respuesta esperada:** `200 OK` conteniendo el objeto `ticket` y su `history` completo.

---

## 4. Consultas SQL de Verificación en Base de Datos

Ejecute estas consultas en `postgres_local` para verificar la persistencia y trazabilidad de los tickets:

### 1. Consultar estado del ticket e información operativa:
```sql
SELECT id, folio, title, category, status, priority, assigned_technician, house_number, reporter_name, created_at, closed_at
FROM amenities_schema.maintenance_tickets
ORDER BY created_at DESC;
```

### 2. Auditar la línea de tiempo y bitácora de actualizaciones:
```sql
SELECT u.author_name, u.author_role, u.previous_status, u.new_status, u.notes, u.created_at
FROM amenities_schema.ticket_updates u
WHERE u.ticket_id = '<ID_DEL_TICKET>'
ORDER BY u.created_at ASC;
```
