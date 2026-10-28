---
proyecto: Sistema-Gestion-Residencial
fecha: 2026-10-27
tipo: qa-report
tags: [qa, unit-testing, us-301, us-302, spring-boot, mockito, mockmvc, amenities-service, bookings]
---

# QA Unit Testing Specification & Execution Report: Épica 3 (US-301 & US-302)

## 1. Resumen Ejecutivo y Alcance
Este reporte documenta la suite de pruebas unitarias automatizadas para la **Épica 3: Áreas Comunes y Calendario de Reservas (US-301 y US-302)** implementada en el microservicio `amenities-service` (Spring Boot 3.3.4, Java 21 LTS).

Las pruebas validan de forma aislada e independiente con **JUnit 5 Jupiter**, **Mockito** y **Spring WebMvcTest**:
- **US-301:** Configuración de amenidades, validación de horarios de operación, capacidad de aforo y catálogo de eventos permitidos.
- **US-301 Invariantes:** Rechazo de códigos duplicados con `409 Conflict` (`DuplicateAmenityCodeException`), rechazo de aforos `<= 0` y tarifas negativas.
- **US-302:** Creación de reservas para casas solventes, bloqueo de reservas para casas morosas con `403 Forbidden` (`DelinquentHouseException`).
- **US-302 Invariantes:** Detección y bloqueo de colisiones horarias (`409 Conflict` / `BookingConflictException`) y rechazo de eventos no autorizados para el área.
- **US-302 Privacidad:** Ocultación de nombres de residentes y tipos de evento en la consulta de calendario, mostrando estrictamente `"Reservado"`.

---

## 2. Componentes y Casos Evaluados

| Capa | Clase de Prueba | Escenarios Evaluados | Resultado |
| :--- | :--- | :--- | :---: |
| **Web** | `AmenityControllerTest` | - Creación exitosa de amenidad (201 Created)<br>- Conflicto por código duplicado (409 Conflict)<br>- Consulta de calendario con privacidad (200 OK) | **3 / 3 PASSED** |
| **Web** | `BookingControllerTest` | - Creación exitosa de reserva (201 Created)<br>- Bloqueo de casa morosa (403 Forbidden)<br>- Conflicto por solapamiento de horario (409 Conflict) | **3 / 3 PASSED** |
| **Application** | `ConfigureAmenityUseCaseTest` | - Configuración exitosa con guardado<br>- Rechazo de código duplicado<br>- Validación de request no nulo | **3 / 3 PASSED** |
| **Application** | `CreateBookingUseCaseTest` | - Reserva exitosa para casa solvente<br>- Bloqueo a casa morosa con `DelinquentHouseException`<br>- Detección de solapamiento de fechas y horarios | **3 / 3 PASSED** |
| **Application** | `GetAmenityCalendarUseCaseTest` | - Regla de privacidad: Mapeo de slots ocupados a `"Reservado"` | **1 / 1 PASSED** |
| **Domain** | `AmenitiesDomainTest` | - Invariantes de construcción de `Amenity`<br>- Verificación de horario de apertura y tipos de evento<br>- Invariantes de `Booking` y algoritmo de solapamiento (`overlapsWith`) | **3 / 3 PASSED** |

---

## 3. Métrica de Ejecución

* **Total de Pruebas Unitarias Ejecutadas:** 16 pruebas automatizadas.
* **Tasa de Aprobación:** 100% (16 aprobadas, 0 fallos, 0 errores).
* **Tiempo de Ejecución:** ~2.1 segundos.
* **Comando para Ejecución Rápida:**
  ```bash
  cd services/amenities-service && mvn test
  ```
