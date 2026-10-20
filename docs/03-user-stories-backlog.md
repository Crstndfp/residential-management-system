---
proyecto: Sistema-Gestion-Residencial
fecha: 2026-10-11
tipo: arquitectura
tags: [historias-de-usuario, backlog, azure-devops, agile, epicas, criterios-de-aceptacion]
---

# Backlog de Historias de Usuario — Sistema de Gestión Residencial

Estructura de backlog lista para gestión ágil (Azure DevOps / Jira), organizada por **Épicas**, **Historias de Usuario (US)** con el formato estándar de la industria y **Criterios de Aceptación (Given-When-Then / Gherkin)**.

---

## Épica 1: Identidad, Roles, Onboarding y Auditoría (Identity & Governance)

### US-101: Onboarding y Asignación de Propietario por la Junta Directiva
* **Como:** Presidente o Vicepresidente de la Junta Directiva (desde la Web).
* **Quiero:** Registrar una nueva Casa (número de identificación, manzana) y asociarle un usuario Propietario mediante verificación manual con envío de invitación por correo.
* **Para:** Garantizar la seguridad jurídica del residencial y asegurar que solo exista exactamente un Propietario por inmueble.
* **Criterios de Aceptación:**
  - **Given** que el Presidente ha iniciado sesión en la Web de administración.
  - **When** registra la Casa "M-12 L-05" e ingresa el nombre y correo del Propietario legítimo.
  - **Then** el sistema persiste la Casa y genera un token de invitación único con vigencia de 48 horas enviado por correo.
  - **And** si se intenta registrar un segundo Propietario para la misma casa, el sistema bloquea la acción con un error de dominio: `"La vivienda ya cuenta con un propietario asignado"`.
  - **And** se registra la acción en la tabla `audit_logs` con `created_by_user_id` y timestamp UTC.

### US-102: Activación de Cuenta y Creación de Inquilino por el Propietario
* **Como:** Propietario de una vivienda (desde la App Android).
* **Quiero:** Invitar y dar de alta a un usuario con rol "Inquilino" para mi casa.
* **Para:** Permitirle el acceso a gestiones del hogar (reservas y tickets) sin darle acceso a finanzas ni derecho a voto.
* **Criterios de Aceptación:**
  - **Given** que un Propietario autenticado accede a la sección "Mi Hogar" en la App Android.
  - **When** ingresa el nombre y correo de su inquilino y confirma el registro.
  - **Then** el sistema crea la cuenta con rol `INQUILINO` asociada a la misma `casa_id`.
  - **And** la aplicación Android para este usuario bloquea de forma visible y por backend el acceso a: estados de cuenta, avisos de cobro y votaciones en asambleas.

---

## Épica 2: Finanzas, Gastos, Conciliación y Mobiliario (Finance & Assets)

### US-201: Registro de Facturas y Egresos Operativos
* **Como:** Tesorero o Administrador Operativo (desde la Web).
* **Quiero:** Registrar las facturas de gastos fijos y variables (energía eléctrica, seguridad, mantenimiento, agua) adjuntando soporte digital (PDF/imagen).
* **Para:** Mantener una bitácora transparente de salidas de dinero del residencial.
* **Criterios de Aceptación:**
  - **Given** que el usuario con rol Tesorero o Admin ingresa al módulo de Egresos.
  - **When** captura número de factura, proveedor, categoría de gasto, fecha de emisión/pago, monto en `NUMERIC(12,2)` y carga el archivo digital.
  - **Then** la factura queda guardada con estado `PAGADO` o `PENDIENTE`.
  - **And** el monto se suma automáticamente a la partida de egresos del mes correspondiente.

### US-202: Conciliación Bancaria de Cuotas mediante Carga de Archivo Excel
* **Como:** Tesorero o Administrador (desde la Web).
* **Quiero:** Cargar el archivo de estado de cuenta bancario (Excel/CSV) que entrega la entidad recaudadora.
* **Para:** Acreditar masivamente los pagos de cuotas de mantenimiento a las casas de forma desatendida.
* **Criterios de Aceptación:**
  - **Given** que el usuario sube una hoja de cálculo bancaria.
  - **When** el motor de conciliación procesa el archivo.
  - **Then** identifica cada fila por número de autorización bancaria y referencia/identificador de casa.
  - **And** si la autorización ya existe en la base de datos, descarta la fila evitando abonos duplicados.
  - **And** cada pago conciliado acredita el saldo en el estado de cuenta de la casa correspondiente.
  - **And** el sistema presenta un resumen visual: *X pagos procesados exitosamente, Y registros con inconsistencias*.

### US-203: Configuración y Aplicación de Multas a Casas
* **Como:** Miembro de la Junta Directiva (desde la Web).
* **Quiero:** Configurar un catálogo de infracciones y aplicar una multa a una casa específica adjuntando evidencia.
* **Para:** Sancionar faltas al reglamento interno y reflejar la deuda en el estado de cuenta del infractor.
* **Criterios de Aceptación:**
  - **Given** que se detecta una infracción (ej. ruidos fuera de horario o exceso de velocidad).
  - **When** la Junta aplica la multa a la casa seleccionando el tipo de infracción, monto y evidencia.
  - **Then** el monto de la multa se suma al saldo adeudado de la casa inmediatamente.
  - **And** se dispara una notificación push clasificada como `FINANCIERA` exclusivamente al Propietario del inmueble.

### US-204: Consulta de Estado de Cuenta Personal (App Android)
* **Como:** Propietario (desde la App Android).
* **Quiero:** Visualizar mi saldo actual, histórico de pagos, desglose de cuotas y multas pendientes.
* **Para:** Tener certeza de mis pagos y saber si me encuentro solvente o en mora.
* **Criterios de Aceptación:**
  - **Given** que un Propietario inicia sesión en la App Android.
  - **When** abre la sección "Finanzas / Mi Estado de Cuenta".
  - **Then** visualiza: Saldo pendiente consolidado, semáforo de solvencia (Al día / En mora) y lista cronológica de cargos y abonos.
  - **And** un usuario con rol `INQUILINO` no tiene visible esta opción en el menú.

### US-205: Inventario de Mobiliario y Equipo del Residencial
* **Como:** Administrador Operativo o Junta Directiva (desde la Web).
* **Quiero:** Registrar y categorizar los activos fijos del residencial (bombas de agua, mobiliario de casa club, cortadoras de césped).
* **Para:** Mantener el control patrimonial del equipamiento y su estado físico.
* **Criterios de Aceptación:**
  - **Given** que el usuario accede al módulo de Activos.
  - **When** registra un activo con código, nombre, ubicación física, costo de adquisición y estado (`Excelente`, `Requiere Mantenimiento`, `Dañado`).
  - **Then** queda indexado en el inventario con historial de modificaciones.

---

## Épica 3: Áreas Comunes y Calendario de Reservas (Amenities)

### US-301: Configuración de Amenidades y Horarios por la Junta Directiva
* **Como:** Miembro de la Junta Directiva (desde la Web).
* **Quiero:** Dar de alta amenidades (Casa Club, Kiosco, Piscina), definir sus horarios de uso, costo de alquiler y la lista de tipos de evento permitidos.
* **Para:** Establecer las reglas claras de alquiler de los espacios compartidos.
* **Criterios de Aceptación:**
  - **Given** que un miembro de la Junta accede a "Configuración de Amenidades".
  - **When** crea o edita un área común asignando costo de reserva, horarios válidos y tipos de evento (Cumpleaños, Boda, Deportivo).
  - **Then** las reglas quedan activas inmediatamente para el calendario de reservas.

### US-302: Consulta de Calendario y Solicitud de Apartado con Cobro Automático
* **Como:** Residente (Propietario o Inquilino desde Web o Android).
* **Quiero:** Visualizar la disponibilidad del área común en un calendario y solicitar un apartado.
* **Para:** Utilizar la amenidad sin conflictos de fechas y con privacidad frente a otros vecinos.
* **Criterios de Aceptación:**
  - **Given** que el residente consulta el calendario de la Casa Club.
  - **When** visualiza los bloques, las fechas ocupadas aparecen únicamente con la etiqueta `"Reservado"` (sin nombres ni tipo de evento).
  - **When** selecciona un bloque disponible, tipo de evento y confirma la reserva.
  - **Then** el sistema valida que la casa no esté en mora.
  - **And** si la validación es exitosa, se bloquea la fecha en el calendario.
  - **And** el costo del alquiler se carga automáticamente al Estado de Cuenta de la casa.
  - **And** se despacha una notificación push a la app confirmando la reserva y el cargo generado.

---

## Épica 4: Asambleas, Quórum y Votaciones Exclusivas Android (Governance)

### US-401: Convocatoria de Asamblea y Pase de Lista Digital
* **Como:** Secretario o Presidente de la Junta Directiva (desde la Web).
* **Quiero:** Crear una Asamblea en el calendario general con su orden del día y gestionar el pase de lista de las casas.
* **Para:** Establecer el quórum legal en tiempo real y emitir el reporte oficial de asistencia.
* **Criterios de Aceptación:**
  - **Given** que el Secretario convoca a una Asamblea Ordinaria en el sistema Web.
  - **When** se celebra la asamblea y utiliza el formulario de asistencia marcando las casas presentes.
  - **Then** el sistema calcula en tiempo real el porcentaje de quórum sobre el total de viviendas.
  - **And** permite generar y exportar el Reporte Oficial de Asistencia en PDF.

### US-402: Emisión de Voto en Asamblea Exclusivo en Android (1 Voto por Casa)
* **Como:** Propietario habilitado (desde la App Android).
* **Quiero:** Emitir mi voto en las mociones de la asamblea activa directamente desde mi teléfono.
* **Para:** Participar democráticamente con validez jurídica e inmutabilidad.
* **Criterios de Aceptación:**
  - **Given** que una votación ha sido abierta por la Junta Directiva.
  - **When** el Propietario ingresa a la moción en su App Android y selecciona su voto (A Favor, En Contra, Abstención).
  - **Then** el sistema registra el voto asociándolo a la `casa_id`, guarda la auditoría (timestamp UTC, hash del voto, IP del móvil).
  - **And** el botón de votar queda bloqueado permanentemente para esa casa en esa pregunta específica (imposible duplicar voto).
  - **And** si un usuario con rol `INQUILINO` intenta abrir la votación, la app le muestra: `"Módulo restringido únicamente a propietarios"`.

### US-403: Cierre de Votación y Reporte Oficial de Resultados
* **Como:** Presidente o Secretario (desde la Web).
* **Quiero:** Cerrar formalmente una votación de asamblea y generar el reporte consolidado.
* **Para:** Dejar constancia formal e inalterable de las decisiones comunitarias aprobadas.
* **Criterios de Aceptación:**
  - **Given** que ha concluido el tiempo de deliberación de una moción.
  - **When** el Secretario pulsa "Cerrar Votación".
  - **Then** el backend bloquea cualquier nuevo intento de inserción en `votos_emitidos`.
  - **And** se genera el Acta/Reporte de Votación (PDF) con el desglose de votos, abstenciones, porcentaje de aprobación y veredicto final.

---

## Épica 5: Tickets de Mantenimiento y Notificaciones (Operations)

### US-501: Levantamiento de Ticket de Incidencia con Fotografías
* **Como:** Residente (Propietario o Inquilino desde Web o Android).
* **Quiero:** Reportar un desperfecto o necesidad de mantenimiento en áreas comunes seleccionando categoría y adjuntando foto.
* **Para:** Que la administración atienda oportunamente el problema físico del residencial.
* **Criterios de Aceptación:**
  - **Given** que un residente detecta una lámpara rota en la calle principal.
  - **When** llena el formulario de ticket con título, categoría (`Alumbrado Público`), ubicación y fotografía.
  - **Then** el ticket se genera con folio único y estado inicial `ABIERTO`.
  - **And** se lista en el panel de incidencias de la Junta Directiva y Administrador Operativo.

### US-502: Seguimiento de Avance y Notificaciones Push al Residente
* **Como:** Administrador Operativo o Vocal de la Junta (desde la Web).
* **Quiero:** Actualizar el estado del ticket (`En Proceso`, `Resuelto`, `Cerrado`) y agregar notas de seguimiento.
* **Para:** Mantener informada a la comunidad y dar trazabilidad a los trabajos de los técnicos.
* **Criterios de Aceptación:**
  - **Given** que el administrador asigna un técnico y pasa el ticket a estado `EN_PROCESO`.
  - **When** se guarda la actualización con un comentario explicativo.
  - **Then** el sistema despacha una notificación push a la App Android del usuario que abrió el ticket avisando del cambio de estado.

---

## Épica 6: Parqueo de Visitas y Analítica de Placas (Parking & Security)

### US-601: Registro Operativo de Visitas en Garita (Tablet Web)
* **Como:** Guardia de Seguridad en Garita (desde la Tablet).
* **Quiero:** Registrar el ingreso y salida de un vehículo visitante (placa, casa destino, nombre y documento de identificación).
* **Para:** Asignarle una plaza de parqueo de visitas disponible y llevar el control de aforo en tiempo real.
* **Criterios de Aceptación:**
  - **Given** que un vehículo particular arriba a la garita para ingresar al residencial.
  - **When** el guardia ingresa placa (ej. "P-123XYZ"), documento y casa a visitar.
  - **Then** el sistema asigna el primer cajón de parqueo de visitas libre (ej. "P-04").
  - **And** dispara una notificación push inmediata al Propietario de la casa visitada: `"El vehículo placa P-123XYZ ha ingresado a su vivienda"`.
  - **When** el vehículo se retira, el guardia marca su salida y el sistema calcula las horas de estadía liberando el cajón.

### US-602: Motor de Analítica de Pernoctas y Detección de "Residente Encubierto"
* **Como:** Miembro de la Junta Directiva (desde la Web).
* **Quiero:** Consultar el mapa de comportamiento y alertas de placas con pernocta recurrente en parqueos de visitas.
* **Para:** Identificar residentes que utilizan el parqueo de visitas como estacionamiento permanente de sus vehículos extras.
* **Criterios de Aceptación:**
  - **Given** que una placa de vehículo ingresa en horario nocturno (20:00 a 23:00) y sale en horario matutino (05:00 a 08:00) más de 3 veces en una semana.
  - **When** el motor de analítica evalúa las estadísticas semanales.
  - **Then** etiqueta la placa como `"SOSPECHA DE RESIDENTE ENCUBIERTO"` y la despliega en el panel de auditoría de parqueos con su historial cronológico.

### US-603: Aplicación de Multa por Uso Indebido de Parqueo
* **Como:** Presidente o Administrador (desde la Web).
* **Quiero:** Aplicar una sanción a la casa vinculada a la placa infractora con un solo clic adjuntando la bitácora de horarios.
* **Para:** Frenar el abuso de los espacios comunes de estacionamiento.
* **Criterios de Aceptación:**
  - **Given** que el administrador valida el reporte de pernocta de la placa infractora.
  - **When** pulsa "Aplicar Multa por Uso Indebido de Parqueo".
  - **Then** el sistema genera la multa con la bitácora adjunta como evidencia irrefutable.
  - **And** el importe se carga automáticamente al Estado de Cuenta de la casa responsable.
  - **And** se despacha una notificación push formal al Propietario del inmueble.
