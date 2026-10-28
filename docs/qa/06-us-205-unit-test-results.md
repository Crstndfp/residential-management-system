---
proyecto: Sistema-Gestion-Residencial
fecha: 2026-10-27
tipo: qa-report
tags: [qa, unit-testing, us-205, spring-boot, mockito, mockmvc, finance-service, fixed-assets]
---

# QA Unit Testing Specification & Execution Report: US-205

## 1. Resumen Ejecutivo y Alcance
Este reporte documenta la suite de pruebas unitarias automatizadas para **US-205** (Inventario de Mobiliario y Equipo del Residencial) dentro del microservicio `finance-service` (Spring Boot 3.3.4, Java 21 LTS).

Las pruebas validan de forma 100% aislada con **JUnit 5 Jupiter**, **Mockito** y **Spring WebMvcTest**:
- Registro de activos con normalización a mayúsculas del código.
- Invariante de unicidad: Rechazo de códigos duplicados con `409 Conflict` (`DuplicateAssetCodeException`).
- Actualización de estado físico (`AssetStatus`) con generación automática del registro de auditoría (`AssetStatusHistory`).
- Manejo de excepciones RFC 7807 para activos inexistentes (`404 Not Found`).
- Listado con filtros opcionales de categoría y estado.
- Invariantes de dominio (POJO DDD): costo estrictamente positivo, campos requeridos no nulos ni vacíos.

---

## 2. Componentes y Casos Evaluados

| Capa | Clase de Prueba | Escenarios Evaluados | Resultado |
| :--- | :--- | :--- | :---: |
| **Web** | `FixedAssetControllerTest` | - Registro exitoso (201 Created)<br>- Rechazo de código duplicado (409 Conflict)<br>- Actualización de estado (200 OK)<br>- Activo no encontrado (404 Not Found)<br>- Consulta con filtros (200 OK) | **5 / 5 PASSED** |
| **Application** | `RegisterFixedAssetUseCaseTest` | - Registro exitoso con mapeo DTO<br>- Detección y lanzamiento de `DuplicateAssetCodeException`<br>- Validación de request no nulo | **3 / 3 PASSED** |
| **Application** | `UpdateAssetStatusUseCaseTest` | - Transición de estado y persistencia de `AssetStatusHistory`<br>- Rechazo de actualización para ID inexistente | **2 / 2 PASSED** |
| **Domain** | `FinanceDomainTest` | - Invariantes de creación de `FixedAsset`<br>- Normalización de código y actualización de estado<br>- Validación de campos obligatorios<br>- Invariantes de `AssetStatusHistory` | **3 / 3 PASSED** |

---

## 3. Métrica de Ejecución

* **Total de Pruebas Unitarias Ejecutadas:** 13 pruebas específicas de US-205 (41 en total en `finance-service`).
* **Tasa de Aprobación:** 100% (0 errores, 0 fallos).
* **Tiempo de Ejecución:** ~2.3 segundos.
* **Comando para Ejecución Rápida:**
  ```bash
  cd services/finance-service && mvn test -Dtest=RegisterFixedAssetUseCaseTest,UpdateAssetStatusUseCaseTest,FixedAssetControllerTest,FinanceDomainTest
  ```
