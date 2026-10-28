# QA Test Plan & Acceptance Matrix: Épica 3 — Amenidades y Reservas (US-301 & US-302)

Este documento proporciona a los ingenieros de QA y testers la especificación técnica completa, matriz de aceptación, guía paso a paso y verificación en base de datos para validar las historias de usuario **US-301** y **US-302**.

---

## 1. Alcance y Criterios de Aceptación (Gherkin)

### US-301: Configuración de Amenidades y Horarios por la Junta Directiva
* **Como:** Miembro de la Junta Directiva (desde la Web).
* **Quiero:** Dar de alta amenidades (Casa Club, Kiosco, Piscina, Canchas), definir sus horarios de uso, costos de alquiler/depósito y tipos de evento permitidos.
* **Para:** Establecer las reglas claras de uso de los espacios compartidos.
* **Criterios de Aceptación:**
  - **Given** que un miembro de la Junta accede a "Configuración de Amenidades".
  - **When** crea o edita un área común asignando costo de reserva, horarios válidos y tipos de evento permitidos (ej. `BIRTHDAY,WEDDING,FAMILY_GATHERING`).
  - **Then** las reglas quedan activas inmediatamente para el calendario de reservas en `amenities_schema.amenities`.
  - **And** si se intenta registrar una amenidad con un código ya existente, el sistema responde `409 Conflict` (`DUPLICATE_AMENITY_CODE`).

### US-302: Consulta de Calendario y Solicitud de Apartado con Cobro Automático
* **Como:** Residente (Propietario o Inquilino desde Web o App Android).
* **Quiero:** Visualizar la disponibilidad del área común en un calendario y solicitar un apartado.
* **Para:** Utilizar la amenidad sin conflictos de fechas y con privacidad frente a otros vecinos.
* **Criterios de Aceptación:**
  - **Given** que el residente consulta el calendario de una amenidad.
  - **When** visualiza los bloques ocupados, aparecen **únicamente con la etiqueta `"Reservado"`** (regla de privacidad estricta: sin nombres ni tipo de evento visible a terceros).
  - **When** selecciona un bloque disponible, tipo de evento permitido y confirma la reserva.
  - **Then** el sistema valida que la casa no se encuentre en mora (delinquent status).
  - **And** si la casa es morosa, la reserva se bloquea con `403 Forbidden` (`HOUSE_DELINQUENT_RESERVATION_BLOCKED`).
  - **And** si la validación es exitosa y el horario no colisiona con otra reserva confirmada, se bloquea la fecha en el calendario (`CONFIRMED`).
  - **And** el costo del alquiler se carga automáticamente y queda asociado a un ID de cargo financiero (`finance_charge_id`).

---

## 2. Matriz de Pruebas y Casos de Aceptación

| Caso ID | Historia | Escenario / Objetivo | Método / Endpoint | Payload de Entrada | Código Esperado | Estado Esperado en BD / Sistema |
| :--- | :---: | :--- | :---: | :--- | :---: | :--- |
| **TC-301-01** | US-301 | Configuración exitosa de nueva amenidad | `POST /api/v1/amenities` | `code: "TENNIS_COURT"`, `capacity: 4`, `rentalCost: 100.00`, `allowedEventTypes: "SPORTS_MATCH,TRAINING"` | **201 Created** | Amenidad creada con código normalizado a mayúsculas, activo por defecto. |
| **TC-301-02** | US-301 | Invariante: Rechazar código de amenidad duplicado | `POST /api/v1/amenities` | Mismo `code: "CLUB_HOUSE"` ya existente | **409 Conflict** | Error `DUPLICATE_AMENITY_CODE`. No se permite duplicidad. |
| **TC-301-03** | US-301 | Invariante: Rechazar capacidad <= 0 o costos negativos | `POST /api/v1/amenities` | `capacity: 0`, `rentalCost: -10.00` | **400 Bad Request** | Error de validación Bean Validation. |
| **TC-301-04** | US-301 | Actualización de parámetros de amenidad | `PUT /api/v1/amenities/{id}` | `rentalCost: 550.00`, `capacity: 130` | **200 OK** | Parámetros actualizados en base de datos. |
| **TC-302-01** | US-302 | Reserva exitosa de amenidad para casa solvente | `POST /api/v1/amenities/bookings` | `amenityId`, `houseId` solvente, `eventType: "BIRTHDAY"`, `bookingDate: Futuro`, `startTime: "14:00"`, `endTime: "18:00"` | **201 Created** | Reserva en estado `CONFIRMED`, `totalCost` asignado y `finance_charge_id` generado. |
| **TC-302-02** | US-302 | Invariante: Bloqueo de reserva para casa en mora (Delinquent) | `POST /api/v1/amenities/bookings` | `houseId` con estado en mora | **403 Forbidden** | Error `HOUSE_DELINQUENT_RESERVATION_BLOCKED`. Reserva no creada. |
| **TC-302-03** | US-302 | Invariante: Prevención de colisión horaria (Overlap) | `POST /api/v1/amenities/bookings` | Mismo horario o bloque solapado (ej. 15:00 a 19:00 sobre 14:00 a 18:00) | **409 Conflict** | Error `BOOKING_SLOT_CONFLICT`. No se permite doble reserva. |
| **TC-302-04** | US-302 | Invariante: Rechazar tipo de evento no permitido | `POST /api/v1/amenities/bookings` | `eventType: "HEAVY_CONCERT"` no configurado en la amenidad | **400 Bad Request** | Error de dominio: tipo de evento no permitido. |
| **TC-302-05** | US-302 | Privacidad de Calendario: Solo mostrar "Reservado" | `GET /api/v1/amenities/{id}/calendar?startDate=...&endDate=...` | Rango de fechas que contiene reservas activas | **200 OK** | Array de slots donde `displayStatus` es estrictamente `"Reservado"`, sin nombres de vecinos. |

---

## 3. Guía Paso a Paso para Testers (Ejecución Manual / Postman / cURL)

### Requisitos Previos:
1. Microservicio `amenities-service` ejecutándose en el puerto `8083`.
2. Microservicio `finance-service` ejecutándose en el puerto `8082` (o cliente de solvencia configurado).
3. Base de datos PostgreSQL con migraciones `V1` a `V6` aplicadas.

### Paso 1: Configurar Nueva Amenidad (Junta Directiva - US-301)
```bash
curl -X POST http://localhost:8083/api/v1/amenities \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SQUASH_COURT",
    "name": "Cancha de Squash Climatizada",
    "description": "Cancha techada con piso de duela y graderíos",
    "capacity": 6,
    "rentalCost": 60.00,
    "depositCost": 40.00,
    "openingTime": "06:00:00",
    "closingTime": "21:00:00",
    "allowedEventTypes": "SPORTS_MATCH,TRAINING"
  }'
```
* **Respuesta Esperada:** HTTP 201 Created con el objeto de la amenidad creada.

### Paso 2: Consultar Calendario Disponible con Privacidad (US-302)
```bash
# Obtener ID de Casa Club
CLUBHOUSE_ID=$(curl -s http://localhost:8083/api/v1/amenities | grep -o '"id":"[^"]*' | head -1 | cut -d: -f2 | tr -d '"')

curl -X GET "http://localhost:8083/api/v1/amenities/$CLUBHOUSE_ID/calendar?startDate=2026-11-01&endDate=2026-11-30"
```
* **Respuesta Esperada:** HTTP 200 OK con lista de slots donde cualquier reserva previa muestra `displayStatus: "Reservado"`.

### Paso 3: Realizar una Reserva para una Casa Solvente (US-302)
```bash
curl -X POST http://localhost:8083/api/v1/amenities/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "amenityId": "'"$CLUBHOUSE_ID"'",
    "houseId": "e2a9c3b0-028c-436d-827d-9525d9a94b4f",
    "houseNumber": "M-01 L-01",
    "residentUserId": "00000000-0000-0000-0000-000000000001",
    "residentName": "Carlos Mendoza",
    "eventType": "BIRTHDAY",
    "bookingDate": "2026-11-15",
    "startTime": "14:00:00",
    "endTime": "18:00:00"
  }'
```
* **Respuesta Esperada:** HTTP 201 Created con `status: "CONFIRMED"` y `totalCost: 500.00`.

### Paso 4: Validar Prevención de Solapamiento Horario (Conflict Overlap)
```bash
# Intentar reservar en el mismo día solapando horario (16:00 a 20:00)
curl -X POST http://localhost:8083/api/v1/amenities/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "amenityId": "'"$CLUBHOUSE_ID"'",
    "houseId": "acafafef-9a84-4b96-968c-ed0731959310",
    "houseNumber": "M-01 L-02",
    "residentUserId": "00000000-0000-0000-0000-000000000002",
    "residentName": "Vecino Concurrente",
    "eventType": "BIRTHDAY",
    "bookingDate": "2026-11-15",
    "startTime": "16:00:00",
    "endTime": "20:00:00"
  }'
```
* **Respuesta Esperada:** HTTP 409 Conflict con `errorCode: "BOOKING_SLOT_CONFLICT"`.

### Paso 5: Validar en PostgreSQL
```sql
SELECT b.id, a.name AS amenity, b.house_number, b.booking_date, b.start_time, b.end_time, b.status, b.total_cost
FROM amenities_schema.bookings b
JOIN amenities_schema.amenities a ON b.amenity_id = a.id
WHERE b.booking_date = '2026-11-15';
```
* **Resultado Esperado:** 1 única fila confirmada sin duplicidad.
