# Sistema de Gestión Residencial — Portal Web Administrativo y de Garita (Fase 2)

## 1. Visión General & Racional Arquitectónico

La **Fase 2** entrega el portal web administrativo y de garita vehicular para el **Sistema de Gestión Residencial**. Desarrollado como una aplicación de una sola página (**SPA**) reactiva y modular utilizando **React 18**, **TypeScript**, **Tailwind CSS** y empaquetada con **Vite**, provee una interfaz unificada protegida mediante **Autenticación Obligatoria y Control de Acceso Basado en Roles (RBAC)** para evitar el acceso no autorizado y la generación de datos no deseados:

1. **Pantalla de Login & Puerta de Enlace (Gatekeeper):** Bloquea todo acceso no autenticado. Valida credenciales corporativas (usuario y contraseña) para guardias, administradores y miembros de junta.
2. **Operador de Garita (Tablet Touch - US-601):** Al autenticarse como guardia, el sistema restringe estrictamente el acceso a la interfaz táctil de garita, ocultando por completo las pestañas de administración general, finanzas y gobernanza para prevenir manipulación indebida.
3. **Junta Directiva & Administrador General:** Tablero integral de control para la gobernanza (asambleas, quórum legal), seguridad analítica (residentes encubiertos), reservas protegidas de amenidades, gestión de incidencias técnicas e inventario financiero.

### Credenciales Preconfiguradas para Demostración

| Perfil | Correo / Usuario | Contraseña | Rol Asignado | Alcance de la Vista |
| :--- | :--- | :--- | :--- | :--- |
| **Guardia de Garita** | `guardia@lasbrisas.gt` | `Garita2026!` | `GUARD` | **Exclusivo Tablet Garita (US-601).** Menús administrativos bloqueados. |
| **Administrador General** | `admin@lasbrisas.gt` | `Admin2026!` | `ADMIN` | Acceso completo (Seguridad, Asambleas, Amenidades, Tickets, Finanzas). |
| **Presidente de Junta** | `junta@lasbrisas.gt` | `Junta2026!` | `BOARD` | Acceso completo y control de asambleas. |

---

## 2. Diagrama de Flujo y Comunicación (Mermaid)

```mermaid
flowchart TD
    User["Usuario (Navegador Web / Tablet)"]
    Login["Pantalla de Login & Autenticación (Email / Password)"]

    subgraph UI_Layer["Capa de Presentación Web (Vite + React 18)"]
        Navbar["Navbar & Estado de Sesión (Logout)"]
        GaritaView["Modo Garita Tablet (US-601)\n- Cuadrícula 20 Espacios\n- Check-in Táctil\n- 1-Click Check-out"]
        AdminView["Modo Junta Directiva (Tabs)\n- Seguridad & Analítica\n- Asambleas & Quórum\n- Amenidades & Calendario\n- Mantenimiento Kanban\n- Finanzas & Conciliación"]
        APIClient["API Client con Fallback Reactivo\n(web/src/services/api.ts)"]
    end

    User --> Login
    Login -->|Rol GUARD| GaritaView
    Login -->|Rol ADMIN / BOARD| AdminView
    Navbar -->|Switch Modo (Solo Admin)| GaritaView
    Navbar -->|Cerrar Sesión| Login
    GaritaView --> APIClient
    AdminView --> APIClient
```

---

## 3. Módulos Implementados

### 3.1. Garita Tablet & Control de Estacionamiento (US-601)
- **Cuadrícula Interactiva de 20 Espacios (`P-01` a `P-20`):** Cada espacio muestra su estado (Verde: Libre, Rojo: Ocupado), placa del vehículo, casa de destino, nombre del visitante y duración del parqueo calculada en tiempo real.
- **Check-in Táctil Rápido:** Modal adaptado para tablet con validación de placa, selección inmediata de espacio disponible y apertura automática simulada.
- **Check-out en 1-Clic:** Botón "Marcar Salida" directo en la tarjeta del espacio o en la tabla detallada de vehículos activos.

### 3.2. Detección de Residentes Encubiertos y Multas (US-602, US-603)
- **Detección Algorítmica ($\ge 3$ Pernoctaciones):** Filtro automático de placas visitantes con recurrencia nocturna sospechosa.
- **Visor Forense de Evidencia:** Modal que desglosa el historial de entradas y salidas previas con marcas temporales exactas.
- **Aplicación de Multa Directa:** Ventana de emisión de sanción (Q. 250.00 predeterminado) que genera el registro de cobro vinculado al módulo financiero.

### 3.3. Asambleas y Quórum Digital (US-401, US-402, US-403)
- **Termómetro Visual de Quórum:** Barra de progreso en vivo con umbral legal del 50%.
- **Pase de Lista Digital:** Entrada rápida para registrar asistencia de copropietarios por número de casa.
- **Mociones y Escrutinio Criptográfico:** Apertura de votación, emisión y cierre con recuento automático (A favor, En contra, Abstenciones) y generación de sello SHA-256 inmutable para auditoría.

### 3.4. Amenidades y Calendario Protegido (US-301, US-302)
- **Catálogo de Áreas Comunes:** Salón Social, Pérgola Churrasqueras y Cancha Sintética con costos, aforos y horarios.
- **Calendario con Privacidad Estricta (US-302):** Las franjas horarias únicamente se exponen como "Disponible" o "Reservado", protegiendo los nombres y datos de los residentes ante terceros.

### 3.5. Mantenimiento y Tickets de Falla (US-501, US-502)
- **Tablero de Incidencias:** Filtro por estados (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`) y etiquetas de prioridad.
- **Asignación y Flujo de Vida:** Actualización de estados, asignación de técnicos especialistas y registro de avances.

### 3.6. Finanzas, Conciliación y Activos (US-201, US-202, US-203, US-205)
- **Facturas con Respaldo Digital:** Listado de egresos operativos con enlaces a comprobantes.
- **Conciliación Bancaria Masiva:** Interfaz de carga para estados de cuenta bancarios en Excel con cálculo de tasa de cruce (95.8%).
- **Inventario Físico de Activos Comunitarios:** Registro de bienes valorizados (talanqueras, bombas, servidores NVR).

---

## 4. Estructura de Archivos en el Repositorio

```
web/
├── Dockerfile                   # Construcción multi-stage (Node 20 + Nginx Alpine)
├── nginx.conf                   # Reverse proxy y soporte de rutas SPA
├── package.json                 # React 18, Lucide React, Tailwind CSS, Vite
├── vite.config.ts               # Configuración Vite con proxy a :8080
├── tailwind.config.js           # Tema oscuro y paleta Slate / Emerald / Blue / Rose
├── src/
│   ├── index.css                # Directivas Tailwind y keyframes de animación
│   ├── main.tsx                 # Punto de entrada React 18
│   ├── App.tsx                  # Componente raíz con estado reactivo y routing de modos
│   ├── types/
│   │   └── index.ts             # Interfaces TypeScript de todos los módulos
│   ├── services/
│   │   └── api.ts               # Cliente API REST y generador de datos mock interactivos
│   └── components/
│       ├── layout/
│       │   └── Navbar.tsx       # Barra de navegación dual (Garita vs. Junta)
│       ├── garita/
│       │   ├── ParkingSpotGrid.tsx    # Cuadrícula interactiva 20 espacios
│       │   └── GateCheckInModal.tsx   # Modal de registro vehicular
│       └── admin/
│           ├── CovertResidentsPanel.tsx   # Alertas de pernoctación y multas
│           ├── AssembliesPanel.tsx        # Quórum y mociones con hash SHA-256
│           ├── AmenitiesCalendarPanel.tsx # Calendario con filtro de privacidad
│           ├── MaintenanceTicketsPanel.tsx# Kanban y flujo de incidencias
│           └── FinanceOverviewPanel.tsx   # Facturas, conciliación Excel y activos
```

---

## 5. Instrucciones de Ejecución

### Modo Desarrollo
```bash
cd web
npm install
npm run dev
```
La aplicación inicia en `http://localhost:3000`.

### Verificación de Compilación
```bash
cd web
npm run build
```

### Ejecución con Docker Compose
```bash
docker compose up web -d
```
Acceder mediante `http://localhost:3000`.
