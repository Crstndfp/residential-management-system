# Sistema de Gestión Residencial

Plataforma de gestión integral comunitaria, financiera y de seguridad para complejos residenciales.

## Arquitectura
* **Microservicios:** Java 21 LTS (OpenJDK) + Spring Boot 3 con enfoque DDD.
* **Base de Datos:** PostgreSQL 16 con esquemas aislados por bounded context.
* **Mensajería:** RabbitMQ.
* **Gateway:** Nginx Reverse Proxy.
* **Mobile:** Android Nativo (Kotlin + Jetpack Compose).
* **Web:** React + TypeScript + Vite.

## Puesta en Marcha Local
```bash
docker compose up -d
```
