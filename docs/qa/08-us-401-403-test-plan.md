# QA Test Plan & Acceptance Matrix: Épica 4 — Asambleas, Quórum y Votaciones Exclusivas (US-401, US-402, US-403)

Este documento proporciona a los ingenieros de QA y testers la especificación técnica completa, matriz de aceptación, guía paso a paso con comandos cURL y verificación en base de datos para validar las historias de usuario **US-401**, **US-402** y **US-403**.

---

## 1. Alcance y Criterios de Aceptación (Gherkin)

### US-401: Convocatoria Digital y Registro de Asistencia (Quórum)
* **Como:** Administrador o Miembro de la Junta Directiva.
* **Quiero:** Convocar a una asamblea general (ordinaria o extraordinaria), definir la orden del día y registrar la asistencia de las casas presentes.
* **Para:** Realizar el pase de lista digital y determinar automáticamente si existe el quórum legal (mínimo 50% de casas habitadas) para sesionar válidamente.
* **Criterios de Aceptación:**
  - **Given** que el administrador convoca a una nueva asamblea (`ORDINARY` o `EXTRAORDINARY`) con fecha, hora, orden del día y número total de casas residenciales.
  - **When** se inicia el evento y los vecinos se acreditan, se registra la asistencia casa por casa (`assembly_attendance`).
  - **Then** el sistema calcula en tiempo real el porcentaje de quórum (`(presentes / total_casas) * 100`) e indica el indicador booleano `hasQuorum` (>= 50.0%).
  - **And** si la asamblea no existe, responde `404 Not Found` (`ASSEMBLY_NOT_FOUND`).

### US-402: Votaciones Exclusivas para Propietarios y Regla de 1 Voto por Casa
* **Como:** Residente Propietario (desde la App Android o Web).
* **Quiero:** Emitir mi voto (A Favor, En Contra o Abstención) en las mociones sometidas a votación durante la asamblea.
* **Para:** Participar en las decisiones comunitarias de forma segura, auditable y democrática.
* **Criterios de Aceptación:**
  - **Given** que una moción se encuentra en estado `OPEN` para votación.
  - **When** un usuario intenta votar con rol de Inquilino (`TENANT`), el sistema bloquea inmediatamente la solicitud con `403 Forbidden` (`TENANT_VOTING_FORBIDDEN`).
  - **When** un Propietario (`OWNER`) emite su voto para una casa, el sistema genera un hash criptográfico de auditoría SHA-256 inmutable (`voteHash`) combinando moción, casa, opción y marca de tiempo.
  - **When** se intenta emitir un segundo voto para la misma casa en la misma moción, el sistema responde `409 Conflict` (`DUPLICATE_VOTE_CAST`), haciendo cumplir la regla estricta de **1 voto por casa**.
  - **And** si se intenta votar en una moción que no está abierta (`PENDING` o `CLOSED`), responde `400 Bad Request` (`MOTION_NOT_OPEN`).

### US-403: Cierre de Votación y Cálculo Automático de Resultados
* **Como:** Administrador o Moderador de la Asamblea.
* **Quiero:** Cerrar formalmente el periodo de votación de una moción.
* **Para:** Obtener el escrutinio automático, porcentaje de aprobación y veredicto final vinculante sin manipulación manual.
* **Criterios de Aceptación:**
  - **Given** que una moción está en estado `OPEN` y ha recibido votos de los propietarios acreditados.
  - **When** el moderador solicita el cierre de la votación (`POST /api/v1/amenities/motions/{id}/close`).
  - **Then** la moción pasa a estado `CLOSED` con timestamp de cierre.
  - **And** se contabilizan automáticamente los votos: `inFavorCount`, `againstCount`, `abstainCount` y `totalVotes`.
  - **And** se calcula el porcentaje de aprobación `(inFavor / totalVotes) * 100`.
  - **And** se asigna el veredicto vinculante:
    - `APPROVED`: Si los votos a favor superan a los votos en contra (`inFavor > against`).
    - `REJECTED`: Si los votos en contra superan a los votos a favor (`against > inFavor`) o si no hubo votos a favor.
    - `TIE`: Si existe empate exacto entre votos a favor y en contra.

---

## 2. Matriz de Pruebas y Casos de Aceptación

| Caso ID | Historia | Escenario / Objetivo | Método / Endpoint | Payload de Entrada | Código Esperado | Estado Esperado en BD / Sistema |
| :--- | :---: | :--- | :---: | :--- | :---: | :--- |
| **TC-401-01** | US-401 | Convocatoria exitosa de asamblea ordinaria | `POST /api/v1/amenities/assemblies` | `title: "Asamblea Anual 2026"`, `assemblyType: "ORDINARY"`, `totalResidentialHouses: 100` | **201 Created** | Asamblea registrada con estado `CONVOKED` en `amenities_schema.assemblies`. |
| **TC-401-02** | US-401 | Registro de asistencia (pase de lista digital) | `POST /api/v1/amenities/assemblies/{id}/attendance` | `houseId`, `houseNumber: "A-101"`, `attendedByUserId`, `attendedByName`, `isPresent: true` | **200 OK** | Asistencia registrada en `amenities_schema.assembly_attendance`. |
| **TC-401-03** | US-401 | Cálculo de quórum con asistencia < 50% | `GET /api/v1/amenities/assemblies/{id}/quorum` | ID de asamblea con 45 casas presentes de 100 | **200 OK** | `quorumPercentage: 45.0`, `hasQuorum: false`. |
| **TC-401-04** | US-401 | Cálculo de quórum con asistencia >= 50% | `GET /api/v1/amenities/assemblies/{id}/quorum` | ID de asamblea con 55 casas presentes de 100 | **200 OK** | `quorumPercentage: 55.0`, `hasQuorum: true`. |
| **TC-401-05** | US-401 | Alta de moción de asamblea | `POST /api/v1/amenities/assemblies/{id}/motions` | `title: "Aprobación Techo Casa Club"`, `description: "Presupuesto $5,000"` | **201 Created** | Moción creada en estado `PENDING` en `amenities_schema.assembly_motions`. |
| **TC-402-01** | US-402 | Apertura formal de moción para votación | `POST /api/v1/amenities/motions/{id}/open` | ID de moción existente | **200 OK** | Moción cambia de `PENDING` a `OPEN`, registra `opened_at`. |
| **TC-402-02** | US-402 | Voto exitoso de propietario con hash SHA-256 | `POST /api/v1/amenities/motions/{id}/vote` | Header `X-User-Role: OWNER`, `houseId`, `voteChoice: "IN_FAVOR"` | **201 Created** | Voto guardado en `motion_votes` con `vote_hash` SHA-256 válido (64 caracteres). |
| **TC-402-03** | US-402 | Invariante: Bloqueo de voto a inquilino (Tenant) | `POST /api/v1/amenities/motions/{id}/vote` | Header `X-User-Role: TENANT`, `houseId`, `voteChoice: "IN_FAVOR"` | **403 Forbidden** | Error `TENANT_VOTING_FORBIDDEN`. No se almacena voto. |
| **TC-402-04** | US-402 | Invariante: Bloqueo de voto en moción cerrada o pendiente | `POST /api/v1/amenities/motions/{id}/vote` | Header `X-User-Role: OWNER` sobre moción `PENDING` | **400 Bad Request** | Error `MOTION_NOT_OPEN`. |
| **TC-402-05** | US-402 | Invariante: Regla de 1 voto por casa (Rechazo duplicado) | `POST /api/v1/amenities/motions/{id}/vote` | Mismo `houseId` intenta emitir un segundo voto | **409 Conflict** | Error `DUPLICATE_VOTE_CAST`. Restricción única respetada. |
| **TC-403-01** | US-403 | Cierre de votación y veredicto APPROVED | `POST /api/v1/amenities/motions/{id}/close` | ID de moción con más votos a favor que en contra | **200 OK** | Estado `CLOSED`, `verdict: "APPROVED"`, `approvalPercentage` calculado con precisión decimal. |
| **TC-403-02** | US-403 | Cierre de votación y veredicto REJECTED | `POST /api/v1/amenities/motions/{id}/close` | ID de moción con más votos en contra que a favor | **200 OK** | Estado `CLOSED`, `verdict: "REJECTED"`. |

---

## 3. Guía Paso a Paso para Testers (Ejecución Manual / Postman / cURL)

### Requisitos Previos:
1. Microservicio `amenities-service` ejecutándose en el puerto `8083`.
2. Base de datos PostgreSQL local corriendo con esquema `amenities_schema` y migración `V7` aplicada.

### Paso 1: Convocar Asamblea General (US-401)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/assemblies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Asamblea General Ordinaria 2026",
    "assemblyType": "ORDINARY",
    "scheduledAt": "2026-11-15T18:00:00Z",
    "agenda": "1. Presentacion estados financieros\n2. Votacion de mejoras al gimnasio\n3. Eleccion de comite de vigilancia",
    "totalResidentialHouses": 100
  }'
```
* **Respuesta esperada:** `201 Created` con el ID de la asamblea y estado `"CONVOKED"`.

---

### Paso 2: Pase de Lista Digital y Registro de Asistencia (US-401)
```bash
ASSEMBLY_ID="<ID_OBTENIDO_EN_PASO_1>"

curl -i -X POST http://localhost:8083/api/v1/amenities/assemblies/${ASSEMBLY_ID}/attendance \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "A-101",
    "attendedByUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "attendedByName": "Carlos Flores",
    "isPresent": true
  }'
```
* **Respuesta esperada:** `200 OK` con registro de asistencia y timestamp de acreditación.

---

### Paso 3: Consultar Quórum Legal en Tiempo Real (US-401)
```bash
curl -i -X GET http://localhost:8083/api/v1/amenities/assemblies/${ASSEMBLY_ID}/quorum
```
* **Respuesta esperada:** `200 OK`
```json
{
  "assemblyId": "...",
  "assemblyTitle": "Asamblea General Ordinaria 2026",
  "assemblyStatus": "CONVOKED",
  "totalResidentialHouses": 100,
  "presentHouses": 1,
  "quorumPercentage": 1.0,
  "hasQuorum": false
}
```

---

### Paso 4: Crear una Moción o Punto de Votación (US-401 / US-402)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/assemblies/${ASSEMBLY_ID}/motions \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Aprobacion del Proyecto de Paneles Solares para Casa Club",
    "description": "Se propone una cuota extraordinaria unica de $50 por casa para instalacion de paneles solares."
  }'
```
* **Respuesta esperada:** `201 Created` con el ID de la moción en estado `"PENDING"`.

---

### Paso 5: Abrir la Moción para Votación (US-402)
```bash
MOTION_ID="<ID_OBTENIDO_EN_PASO_4>"

curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/open
```
* **Respuesta esperada:** `200 OK` con estado `"OPEN"` y timestamp `openedAt`.

---

### Paso 6: Intentar Votar como Inquilino (Prueba de Invariante de Seguridad - US-402)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/vote \
  -H "Content-Type: application/json" \
  -H "X-User-Role: TENANT" \
  -d '{
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "A-101",
    "voterUserId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "voteChoice": "IN_FAVOR"
  }'
```
* **Respuesta esperada:** `403 Forbidden`
```json
{
  "type": "urn:problem-type:tenant-voting-forbidden",
  "title": "Tenant Voting Forbidden",
  "status": 403,
  "detail": "Only property owners can vote in assembly motions. Current role: TENANT",
  "errorCode": "TENANT_VOTING_FORBIDDEN"
}
```

---

### Paso 7: Emitir Voto Válido como Propietario (US-402)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/vote \
  -H "Content-Type: application/json" \
  -H "X-User-Role: OWNER" \
  -d '{
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "A-101",
    "voterUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "voteChoice": "IN_FAVOR"
  }'
```
* **Respuesta esperada:** `201 Created`
```json
{
  "id": "...",
  "motionId": "...",
  "houseId": "11111111-1111-1111-1111-111111111111",
  "houseNumber": "A-101",
  "voterUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "voteChoice": "IN_FAVOR",
  "voteHash": "3f4a9b...64_caracteres_sha256...",
  "createdAt": "..."
}
```

---

### Paso 8: Intentar Votar Doble para la Misma Casa (Prueba de Invariante 1 Voto por Casa - US-402)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/vote \
  -H "Content-Type: application/json" \
  -H "X-User-Role: OWNER" \
  -d '{
    "houseId": "11111111-1111-1111-1111-111111111111",
    "houseNumber": "A-101",
    "voterUserId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    "voteChoice": "AGAINST"
  }'
```
* **Respuesta esperada:** `409 Conflict`
```json
{
  "type": "urn:problem-type:duplicate-vote-cast",
  "title": "Duplicate Vote Cast",
  "status": 409,
  "detail": "House 11111111-1111-1111-1111-111111111111 has already cast a vote for motion ...",
  "errorCode": "DUPLICATE_VOTE_CAST"
}
```

---

### Paso 9: Emitir Segundo Voto Válido con Otra Casa (US-402)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/vote \
  -H "Content-Type: application/json" \
  -H "X-User-Role: OWNER" \
  -d '{
    "houseId": "22222222-2222-2222-2222-222222222222",
    "houseNumber": "B-202",
    "voterUserId": "cccccccc-cccc-cccc-cccc-cccccccccccc",
    "voteChoice": "IN_FAVOR"
  }'
```
* **Respuesta esperada:** `201 Created`

---

### Paso 10: Cerrar la Moción y Obtener Escrutinio Automático (US-403)
```bash
curl -i -X POST http://localhost:8083/api/v1/amenities/motions/${MOTION_ID}/close
```
* **Respuesta esperada:** `200 OK`
```json
{
  "motionId": "...",
  "title": "Aprobacion del Proyecto de Paneles Solares para Casa Club",
  "status": "CLOSED",
  "verdict": "APPROVED",
  "inFavorCount": 2,
  "againstCount": 0,
  "abstainCount": 0,
  "totalVotes": 2,
  "approvalPercentage": 100.0,
  "closedAt": "2026-..."
}
```

---

## 4. Consultas SQL de Verificación en Base de Datos

Ejecute estas consultas en `postgres_local` para auditar la persistencia, integridad referencial y hashes de auditoría:

### 1. Verificar registro y estado de la asamblea:
```sql
SELECT id, title, assembly_type, status, total_residential_houses, scheduled_at, created_at
FROM amenities_schema.assemblies
ORDER BY created_at DESC;
```

### 2. Verificar pase de lista y casas acreditadas:
```sql
SELECT a.house_number, a.attended_by_name, a.is_present, a.checked_in_at
FROM amenities_schema.assembly_attendance a
WHERE a.assembly_id = '<ID_DE_ASAMBLEA>'
ORDER BY a.house_number;
```

### 3. Verificar votos emitidos, opciones y hashes criptográficos de auditoría:
```sql
SELECT v.house_number, v.vote_choice, v.vote_hash, v.created_at
FROM amenities_schema.motion_votes v
WHERE v.motion_id = '<ID_DE_MOCION>'
ORDER BY v.created_at ASC;
```

### 4. Verificar resultado final de la moción cerrada:
```sql
SELECT m.id, m.title, m.status, m.verdict, m.opened_at, m.closed_at
FROM amenities_schema.assembly_motions m
WHERE m.id = '<ID_DE_MOCION>';
```
