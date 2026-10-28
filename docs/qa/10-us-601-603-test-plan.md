# QA Test Plan & Acceptance Matrix: Épica 6 — Parqueo de Visitas y Analítica de Placas (US-601, US-602, US-603)

Este documento proporciona a los ingenieros de QA y testers la especificación técnica completa, matriz de aceptación, guía paso a paso con comandos cURL y verificación en base de datos para validar las historias de usuario **US-601**, **US-602** y **US-603**.

---

## 1. Alcance y Criterios de Aceptación (Gherkin)

### US-601: Registro Operativo de Visitas en Garita (Tablet Web)
* **Como:** Guardia de Seguridad en Garita (desde la Tablet).
* **Quiero:** Registrar el ingreso y salida de un vehículo visitante (placa, casa destino, nombre y documento de identificación).
* **Para:** Asignarle automáticamente una plaza de parqueo de visitas libre, notificar al residente y llevar el control de aforo en tiempo real.
* **Criterios de Aceptación:**
  - **Given** que un vehículo visitante arriba a la garita para ingresar al residencial.
  - **When** el guardia captura la placa (ej. `"P-123XYZ"`), documento de identificación, nombre del conductor y vivienda visitada.
  - **Then** el sistema asigna el primer cajón de parqueo de visitas libre (ej. `"P-01"` a `"P-20"`), lo marca como ocupado y guarda el registro con estado `ACTIVE`.
  - **And** dispara una notificación push inmediata al Propietario de la casa visitada: `"El vehículo placa P-123XYZ ha ingresado a su vivienda y se le asignó el parqueo P-01"`.
  - **And** si todos los cajones de visitas se encuentran ocupados, responde con `409 Conflict` (`NO_AVAILABLE_PARKING_SPOT`).
  - **And** si la placa ya cuenta con un ingreso activo sin salida previa, responde con `409 Conflict` (`PLATE_ALREADY_ACTIVE`).
  - **When** el vehículo se retira, el guardia marca su salida, el sistema calcula la duración de estadía en minutos y horas, libera el cajón de parqueo y marca el registro como `COMPLETED`.

### US-602: Motor de Analítica de Pernoctas y Detección de "Residente Encubierto"
* **Como:** Miembro de la Junta Directiva (desde la Web).
* **Quiero:** Consultar el panel de analítica de comportamiento y alertas de placas con pernocta recurrente en parqueos de visitas.
* **Para:** Identificar residentes o inquilinos que utilizan el parqueo de visitas como estacionamiento permanente de sus vehículos extras sin pagar cuota de parqueo adicional.
* **Criterios de Aceptación:**
  - **Given** que el sistema registra los ingresos y salidas de vehículos.
  - **When** una placa ingresa en horario nocturno (20:00 a 23:59) y sale en horario matutino (05:00 a 08:59) con una estadía mayor o igual a 5 horas, se clasifica automáticamente con `is_overnight: true`.
  - **When** el motor de analítica evalúa la ventana semanal (últimos 7 días) y detecta una placa con 3 o más pernoctas (`overnightCount >= 3`).
  - **Then** etiqueta la placa con `"SOSPECHA DE RESIDENTE ENCUBIERTO"`, asigna nivel de sospecha (`HIGH` o `CRITICAL`) y la presenta en el panel de auditoría con su historial cronológico detallado.

### US-603: Aplicación de Multa por Uso Indebido de Parqueo
* **Como:** Presidente o Administrador de la Junta Directiva (desde la Web).
* **Quiero:** Aplicar una sanción económica a la casa vinculada a la placa infractora con un solo clic, adjuntando la bitácora de horarios de pernocta como evidencia irrefutable.
* **Para:** Frenar el abuso de los espacios comunes de estacionamiento y mantener la disciplina comunitaria.
* **Criterios de Aceptación:**
  - **Given** que el administrador valida en el panel de analítica el reporte de pernocta de la placa sospechosa.
  - **When** pulsa "Aplicar Multa por Uso Indebido de Parqueo" (`POST /api/v1/parking/fines`).
  - **Then** el sistema genera la multa en `parking_schema.parking_fines` con el historial de fechas y horas como evidencia inmutable.
  - **And** el importe (por defecto $50.00 o configurado) se carga al estado de cuenta de la casa en el servicio financiero (`finance_charge_id`).
  - **And** se despacha una notificación push formal al Propietario del inmueble responsable.

---

## 2. Matriz de Pruebas y Casos de Aceptación

| Caso ID | Historia | Escenario / Objetivo | Método / Endpoint | Payload de Entrada | Código Esperado | Estado Esperado en BD / Sistema |
| :--- | :---: | :--- | :---: | :--- | :---: | :--- |
| **TC-601-01** | US-601 | Registro exitoso de entrada de visitante en garita | `POST /api/v1/parking/entries` | `plate: "P-123XYZ"`, `houseNumber: "A-101"`, `visitorName: "Juan"`, `visitorIdDocument: "DPI-1234"` | **201 Created** | Entrada creada en `parking_schema.vehicle_entries` (`ACTIVE`), cajón asignado (ej. `P-01`) marcado como `is_occupied: true` y push despachado. |
| **TC-601-02** | US-601 | Invariante: Rechazar entrada duplicada para placa activa | `POST /api/v1/parking/entries` | Misma placa `"P-123XYZ"` sin haber registrado salida | **409 Conflict** | Error `PLATE_ALREADY_ACTIVE`. No se duplica ingreso. |
| **TC-601-03** | US-601 | Registro exitoso de salida y liberación de cajón | `POST /api/v1/parking/entries/{id}/exit` | ID de entrada activa | **200 OK** | Entrada en `COMPLETED`, `duration_minutes` calculado, cajón liberado (`is_occupied: false`). |
| **TC-601-04** | US-601 | Consulta de aforo y estado de cajones de visita | `GET /api/v1/parking/spots` | Sin parámetros | **200 OK** | Lista de 20 cajones (`P-01` a `P-20`) indicando estado de ocupación. |
| **TC-602-01** | US-602 | Detección heurística de pernocta nocturna | Salida de vehículo | Ingreso a las 21:00 y salida a las 07:00 | **200 OK** | Entrada marcada automáticamente con `is_overnight: true`. |
| **TC-602-02** | US-602 | Alerta de "Residente Encubierto" ($\ge$ 3 pernoctas) | `GET /api/v1/parking/analytics/covert-residents?days=7` | Ventana de 7 días | **200 OK** | Placa clasificada con `label: "SOSPECHA DE RESIDENTE ENCUBIERTO"`, `isSuspect: true` e historial. |
| **TC-603-01** | US-603 | Aplicación de multa con bitácora como evidencia | `POST /api/v1/parking/fines` | `plate: "P-123XYZ"`, `houseId`, `houseNumber: "A-101"`, `fineAmount: 50.00` | **201 Created** | Multa en `parking_fines`, cargo financiero generado y notificación push despachada. |
| **TC-603-02** | US-603 | Consulta de histórico de multas aplicadas | `GET /api/v1/parking/fines?plate=P-123XYZ` | Filtro por placa | **200 OK** | Lista cronológica de multas aplicadas por uso indebido de parqueo. |

---

## 3. Guía Paso a Paso para Testers (Ejecución Manual / Postman / cURL)

### Requisitos Previos:
1. Microservicio `parking-service` ejecutándose en el puerto `8084`.
2. Base de datos PostgreSQL local corriendo con esquema `parking_schema` y migración `V9` aplicada (20 cajones de visita inicializados).

---

### Paso 1: Registrar Entrada de Vehículo Visitante en Garita (US-601)
```bash
curl -i -X POST http://localhost:8084/api/v1/parking/entries \
  -H "Content-Type: application/json" \
  -d '{
    "plate": "P-888ABC",
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "B-102",
    "visitorName": "Carlos Mendoza",
    "visitorIdDocument": "DPI-2456789010101",
    "guardUserId": "99999999-9999-9999-9999-999999999999"
  }'
```
* **Respuesta esperada:** `201 Created`
```json
{
  "id": "...",
  "plate": "P-888ABC",
  "houseId": "11111111-1111-1111-1111-111111111111",
  "houseNumber": "B-102",
  "visitorName": "Carlos Mendoza",
  "visitorIdDocument": "DPI-2456789010101",
  "parkingSpotId": "...",
  "spotNumber": "P-01",
  "guardUserId": "99999999-9999-9999-9999-999999999999",
  "entryTime": "2026-...",
  "exitTime": null,
  "durationMinutes": null,
  "status": "ACTIVE",
  "isOvernight": false,
  "createdAt": "2026-..."
}
```
* **Verificación de logs de backend:**  
  `[PUSH-DISPATCH] Notified Owner of house [11111111-1111-1111-1111-111111111111] (B-102): Vehicle [P-888ABC] entered and was assigned parking spot [P-01]`

---

### Paso 2: Consultar Estado de Ocupación de los Cajones (US-601)
```bash
curl -i -X GET http://localhost:8084/api/v1/parking/spots
```
* **Respuesta esperada:** `200 OK` con el cajón `P-01` en `isOccupied: true` y los restantes en `false`.

---

### Paso 3: Registrar Salida del Vehículo (US-601)
```bash
ENTRY_ID="<ID_DEL_PASO_1>"

curl -i -X POST http://localhost:8084/api/v1/parking/entries/${ENTRY_ID}/exit \
  -H "Content-Type: application/json" \
  -d '{
    "exitTime": "2026-10-28T18:30:00Z"
  }'
```
* **O alternativamente mediante placa:**
```bash
curl -i -X POST "http://localhost:8084/api/v1/parking/entries/exit-by-plate?plate=P-888ABC"
```
* **Respuesta esperada:** `200 OK` con `status: "COMPLETED"`, cálculo de `durationMinutes` y liberación inmediata del cajón `P-01`.

---

### Paso 4: Consultar Analítica de Residentes Encubiertos (US-602)
```bash
curl -i -X GET "http://localhost:8084/api/v1/parking/analytics/covert-residents?days=7"
```
* **Respuesta esperada:** `200 OK`
```json
[
  {
    "plate": "P-888ABC",
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "B-102",
    "overnightCount": 3,
    "isSuspect": true,
    "label": "SOSPECHA DE RESIDENTE ENCUBIERTO",
    "suspicionLevel": "HIGH",
    "firstDetectedEntry": "2026-10-22T21:15:00Z",
    "lastDetectedEntry": "2026-10-27T22:30:00Z",
    "history": [
      {
        "plate": "P-888ABC",
        "entryTime": "2026-10-22T21:15:00Z",
        "exitTime": "2026-10-23T06:30:00Z",
        "durationMinutes": 555,
        "isOvernight": true
      }
    ]
  }
]
```

---

### Paso 5: Aplicar Multa por Uso Indebido de Parqueo (US-603)
```bash
curl -i -X POST http://localhost:8084/api/v1/parking/fines \
  -H "Content-Type: application/json" \
  -d '{
    "plate": "P-888ABC",
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "B-102",
    "fineAmount": 50.00,
    "reason": "Uso indebido reiterado de parqueo de visitas (Residente Encubierto)",
    "appliedByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "appliedByName": "Presidente Junta Directiva"
  }'
```
* **Respuesta esperada:** `201 Created`
```json
{
  "id": "...",
  "plate": "P-888ABC",
  "houseId": "11111111-1111-1111-1111-111111111111",
  "houseNumber": "B-102",
  "fineAmount": 50.00,
  "reason": "Uso indebido reiterado de parqueo de visitas (Residente Encubierto)",
  "evidenceSummary": "Detectadas 3 pernoctas para la placa P-888ABC asociadas a la vivienda B-102...",
  "appliedByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "appliedByName": "Presidente Junta Directiva",
  "financeChargeId": "...",
  "status": "APPLIED",
  "createdAt": "2026-..."
}
```
* **Verificación de logs de backend:**  
  `[FINANCE-CLIENT] Dispatched fine charge [$50.00] to house [11111111-1111-1111-1111-111111111111] - Concept: [Uso indebido reiterado...] (ChargeId: ...)`  
  `[PUSH-DISPATCH] Notified Owner of house [11111111-1111-1111-1111-111111111111] (B-102): Fine applied for [$50.00] on vehicle [P-888ABC] - Reason: ...`

---

## 4. Consultas SQL de Verificación en Base de Datos

Ejecute estas consultas en `postgres_local` para auditar los registros de parqueo:

### 1. Consultar aforo y disponibilidad de plazas:
```sql
SELECT spot_number, spot_type, is_occupied, current_vehicle_entry_id, updated_at
FROM parking_schema.parking_spots
ORDER BY spot_number ASC;
```

### 2. Consultar historial de entradas, duración y pernoctas:
```sql
SELECT plate, house_number, spot_number, visitor_name, entry_time, exit_time, duration_minutes, status, is_overnight
FROM parking_schema.vehicle_entries
ORDER BY entry_time DESC;
```

### 3. Consultar multas por parqueo emitidas y cargos financieros asociados:
```sql
SELECT id, plate, house_number, fine_amount, reason, evidence_summary, finance_charge_id, created_at
FROM parking_schema.parking_fines
ORDER BY created_at DESC;
```
