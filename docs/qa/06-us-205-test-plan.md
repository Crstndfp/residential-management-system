# QA Test Plan & Acceptance Matrix: US-205 (Inventario de Mobiliario y Equipo del Residencial)

Este documento proporciona a los ingenieros de QA y testers la especificación completa, matriz de aceptación y pasos de ejecución detallados para validar **US-205**.

---

## 1. Alcance y Criterios de Aceptación (Gherkin)

* **Historia de Usuario:** US-205 — Inventario de Mobiliario y Equipo del Residencial.
* **Actor Principal:** Administrador Operativo / Miembro de la Junta Directiva (desde Web/API).
* **Objetivo:** Registrar, categorizar y auditar el ciclo de vida y estado físico de los activos fijos del residencial.

### Criterios de Aceptación:
* **Given** que el usuario administrador accede al módulo de Activos Fijos.
* **When** registra un activo con código único (ej. `ACT-PUMP-001`), nombre, categoría (`FACILITY_EQUIPMENT`, `CLUBHOUSE_FURNITURE`, `MAINTENANCE_TOOL`, `SECURITY`), ubicación física, costo de adquisición (`NUMERIC(12,2)` > 0), fecha de compra y estado inicial (`EXCELLENT`, `NEEDS_MAINTENANCE`, `DAMAGED`, `DECOMMISSIONED`).
* **Then** el activo queda persistido en `finance_schema.fixed_assets` con código normalizado en mayúsculas y timestamps UTC.
* **And** si se intenta registrar un activo con un código existente, el sistema responde `409 Conflict` con código de error `DUPLICATE_ASSET_CODE`.
* **When** se actualiza el estado de conservación física de un activo (ej. a `NEEDS_MAINTENANCE` o `DAMAGED`).
* **Then** el nuevo estado se actualiza en el activo y se inserta automáticamente una entrada inmutable de trazabilidad en `finance_schema.asset_status_history` detallando el estado previo, estado nuevo, motivo de la modificación y usuario responsable.
* **When** se consulta el inventario con filtros opcionales de categoría o estado.
* **Then** el sistema retorna únicamente los activos que coinciden con los criterios solicitados.

---

## 2. Matriz de Pruebas y Casos de Aceptación

| Caso ID | Escenario de Prueba | Método / Endpoint | Payload de Entrada / Parámetros | Código Esperado | Estado Esperado en BD / Sistema |
| :--- | :--- | :---: | :--- | :---: | :--- |
| **TC-205-01** | Registro exitoso de activo fijo | `POST /api/v1/finance/assets` | `code: "ACT-PUMP-001"`, `name: "Bomba Sumergible 5HP"`, `category: "FACILITY_EQUIPMENT"`, `location: "Cisterna"`, `acquisitionCost: 12500.00`, `acquisitionDate: "2026-10-01"`, `status: "EXCELLENT"` | **201 Created** | Registro insertado en `finance_schema.fixed_assets`. `id` generado, código en mayúsculas. |
| **TC-205-02** | Registro de activo mobiliario | `POST /api/v1/finance/assets` | `code: "ACT-CHAIR-001"`, `name: "Juego de Sillas"`, `category: "CLUBHOUSE_FURNITURE"`, `acquisitionCost: 7500.00` | **201 Created** | Registro guardado con categoría `CLUBHOUSE_FURNITURE`. |
| **TC-205-03** | Invariante: Rechazar código duplicado | `POST /api/v1/finance/assets` | Mismo `code: "ACT-PUMP-001"` (o en minúsculas `"act-pump-001"`) | **409 Conflict** | Error `DUPLICATE_ASSET_CODE`. No se permite duplicidad de inventario. |
| **TC-205-04** | Invariante: Validación de campos obligatorios y costo positivo | `POST /api/v1/finance/assets` | `code: ""`, `acquisitionCost: -50.00` | **400 Bad Request** | Error de validación Bean Validation en `code` y `acquisitionCost`. |
| **TC-205-05** | Actualización de estado físico y registro de auditoría | `PATCH /api/v1/finance/assets/{id}/status` | `newStatus: "NEEDS_MAINTENANCE"`, `reason: "Fuga en sello mecánico detectada"` | **200 OK** | `fixed_assets.status` cambia a `NEEDS_MAINTENANCE`. Se crea registro en `asset_status_history` con `previous_status='EXCELLENT'` y motivo registrado. |
| **TC-205-06** | Invariante: Actualizar estado de activo inexistente | `PATCH /api/v1/finance/assets/{id_random}/status` | ID no existente | **404 Not Found** | Error `ASSET_NOT_FOUND`. |
| **TC-205-07** | Listado de inventario con filtro por categoría | `GET /api/v1/finance/assets?category=FACILITY_EQUIPMENT` | Parámetro `category` | **200 OK** | Retorna array JSON con los activos coincidentes. |

---

## 3. Guía Paso a Paso para Testers (Ejecución Manual / Postman / cURL)

### Requisitos Previos:
1. Microservicio `finance-service` ejecutándose en `http://localhost:8082`.
2. Base de datos PostgreSQL con migraciones `V1` a `V5` aplicadas (`finance_schema.fixed_assets` y `finance_schema.asset_status_history`).

### Paso 1: Registrar Activo Fijo
```bash
curl -X POST http://localhost:8082/api/v1/finance/assets \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ACT-PUMP-001",
    "name": "Bomba de Agua Sumergible 5HP",
    "category": "FACILITY_EQUIPMENT",
    "physicalLocation": "Cisterna y Pozo Principal",
    "acquisitionCost": 12500.00,
    "acquisitionDate": "2026-10-01",
    "status": "EXCELLENT",
    "notes": "Instalada con garantía de 2 años"
  }'
```
* **Respuesta Esperada:** HTTP 201 Created con el ID generado.

### Paso 2: Validar Rechazo de Código Duplicado
```bash
curl -X POST http://localhost:8082/api/v1/finance/assets \
  -H "Content-Type: application/json" \
  -d '{
    "code": "act-pump-001",
    "name": "Bomba Copia",
    "category": "FACILITY_EQUIPMENT",
    "physicalLocation": "Pozo",
    "acquisitionCost": 5000.00,
    "acquisitionDate": "2026-10-10"
  }'
```
* **Respuesta Esperada:** HTTP 409 Conflict con `errorCode: "DUPLICATE_ASSET_CODE"`.

### Paso 3: Actualizar Estado Físico (Generar Entrada de Historial)
```bash
# Reemplazar {ASSET_ID} por el UUID obtenido en el Paso 1
curl -X PATCH http://localhost:8082/api/v1/finance/assets/{ASSET_ID}/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "NEEDS_MAINTENANCE",
    "reason": "Fuga en sello mecánico detectada en revisión quincenal",
    "changedByUserId": "00000000-0000-0000-0000-000000000001"
  }'
```
* **Respuesta Esperada:** HTTP 200 OK reflejando `status: "NEEDS_MAINTENANCE"`.

### Paso 4: Validar Auditoría en PostgreSQL
```sql
SELECT a.code, a.status, h.previous_status, h.new_status, h.reason, h.created_at
FROM finance_schema.fixed_assets a
JOIN finance_schema.asset_status_history h ON a.id = h.asset_id
WHERE a.code = 'ACT-PUMP-001';
```
* **Resultado Esperado:** 1 fila confirmando la transición `EXCELLENT` -> `NEEDS_MAINTENANCE` con el motivo exacto.
