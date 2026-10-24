# Documento de Arquitectura y Guía Técnica para Desarrolladores
## **ServiClient CRM Helpdesk v1.0.0**

---

## 1. 📌 Visión General del Sistema

**ServiClient CRM Helpdesk** es una plataforma web empresarial diseñada para la gestión integral de relaciones con clientes (CRM) y soporte post-venta (Helpdesk / Mesa de Ayuda) en organizaciones B2B (*Business-to-Business*) que prestan servicios recurrentes.

### Objetivos Clave
- **Gestión 360° de Clientes:** Centralizar la información corporativa, contratos, contactos clave e interacciones históricas.
- **Helpdesk & Control de SLA:** Gestionar solicitudes de soporte con cálculo y seguimiento estricto de tiempos de primera respuesta y resolución acordados.
- **Satisfacción y Salud del Cliente:** Encuestas CSAT automáticas y cálculo continuo del *Health Score* (0 a 100).
- **Retención de Ingresos (ARR):** Alertas tempranas de contratos próximos a expirar para mitigar el riesgo de *churn*.

---

## 2. 🏛️ Arquitectura de Software

### 2.1. Estilo y Patrones Arquitectónicos
- **Patrón Arquitectónico:** Arquitectura en Capas N-Tier con patrón **MVC (Model-View-Controller)**.
- **Aislamiento Multitenancy:** Multitenancy lógico a nivel de fila mediante la columna `empresa_id` en todas las entidades transaccionales.
- **Renderizado:** Server-Side Rendering (SSR) con **Spring Boot + Thymeleaf**.
- **Seguridad:** Autenticación por sesiones HTTP, cifrado BCrypt (fuerza 12) y protección CSRF.
- **Documentación de APIs:** Generación dinámica con **OpenAPI 3.0 / Swagger UI**.

### 2.2. Diagrama de Capas

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE PRESENTACIÓN                           │
│  - Vistas Thymeleaf (HTML5 / Bootstrap 5 / Vanilla JS / Chart.js)       │
│  - Swagger UI (/swagger-ui.html) & OpenAPI Endpoints (/v3/api-docs)     │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE CONTROLADORES                           │
│  - AuthController: Login, Logout, Wizard de Registro en 3 pasos         │
│  - ClienteController: Listado paginado, Alta, Vista 360°, Contactos     │
│  - TicketController: Mesa de ayuda, Conversaciones, SLA, CSAT, Cierre   │
│  - RenovacionController: Panel de vencimientos y Seguimientos           │
│  - GlobalControllerAdvice: Contexto de usuario y menú activo            │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE SEGURIDAD (Spring Security)                 │
│  - SecurityConfig: Reglas de autorización, BCryptPasswordEncoder        │
│  - CustomUserDetailsService & CustomUserDetails: Autenticación JDBC/JPA │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       CAPA DE SERVICIOS / NEGOCIO                       │
│  - ClienteService: CRUD, métricas de salud, KPIs 360°                   │
│  - TicketService: Ciclo de vida, cálculo de SLA, mensajería, CSAT       │
│  - RenovacionService: Detección de contratos por vencer, ARR en riesgo  │
│  - HealthScoreService: Motor de scoring (0-100) y factores de riesgo    │
│  - EmpresaService & UsuarioService: Onboarding y administración         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE ACCESO A DATOS (Spring Data JPA)             │
│  - 14 Interfaces Repository (Spring Data JPA / Hibernate ORM)           │
│  - Consultas JPQL optimizadas y paginación a nivel de base de datos     │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE BASE DE DATOS Y PERSISTENCIA                │
│  - MySQL 8.x: 25 Tablas normalizadas con claves foráneas e índices      │
│  - H2 Database: Base de datos en memoria para testing automatizado      │
│  - DataInitializer: Poblado inicial de datos semilla (seed data)        │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 📦 Estructura del Proyecto

```
com.serviclient.crm
├── config/
│   ├── DataInitializer.java        # Inicialización de datos demo en las 25 tablas
│   ├── OpenApiConfig.java          # Configuración global Swagger 3.0 / OpenAPI
│   └── security/
│       ├── CustomUserDetails.java  # Adaptador UserDetails con empresaId y rol
│       ├── CustomUserDetailsService.java # Carga de usuario por email
│       └── SecurityConfig.java     # Filtros HTTP, BCrypt, CSRF, rutas públicas
├── controller/
│   ├── AuthController.java         # Login y Wizard @SessionAttributes
│   ├── ClienteController.java      # Gestión de clientes y Vista 360°
│   ├── GlobalControllerAdvice.java # Variables globales a vistas Thymeleaf
│   ├── RenovacionController.java   # Panel de renovaciones y seguimientos
│   └── TicketController.java       # Mesa de ayuda y gestión de tickets
├── dto/
│   ├── ClienteDto.java             # Formulario de alta de cliente
│   ├── RegistroEmpresaDto.java     # Wizard de onboarding en 3 pasos
│   └── TicketMensajeDto.java       # DTOs para mensajes, estados y seguimientos
├── entity/
│   ├── Cliente.java / Contacto.java / ClienteServicio.java
│   ├── Empresa.java / RolEntity.java / Permiso.java / Usuario.java
│   ├── Servicio.java / CategoriaTicketEntity.java
│   ├── Ticket.java / TicketMensaje.java / TicketHistorial.java / TicketAdjunto.java
│   ├── EncuestaSatisfaccion.java / Renovacion.java / RenovacionSeguimiento.java
│   ├── Interaccion.java / HealthScoreHistorial.java
│   ├── ConfiguracionSla.java / ConfiguracionCsat.java / ConfiguracionHealthScore.java
│   └── enums/                      # Enums tipados (EstadoTicket, PrioridadTicket, etc.)
├── repository/                     # 14 Repositorios Spring Data JPA
└── service/                        # Lógica transaccional de negocio
```

---

## 4. 🧩 Módulos Funcionales

### 4.1. Autenticación y Onboarding de Empresa
- **Login / Logout:** Autenticación por credenciales corporativas mediante Spring Security.
- **Wizard de Registro en 3 Pasos (`@SessionAttributes`):**
  1. *Paso 1:* Datos de la empresa (Razón social, RUC, industria, país).
  2. *Paso 2:* Datos del usuario administrador principal.
  3. *Paso 3:* Configuración inicial de umbrales SLA, escala CSAT y ponderación del Health Score.

### 4.2. Clientes B2B y Vista 360°
- **Listado y Filtros:** Búsqueda textual y filtrado por estado de salud (`SALUDABLE`, `OBSERVACION`, `EN_RIESGO`).
- **Ficha 360°:** Visión unificada del cliente compuesta por:
  - *Resumen de KPIs:* ARR contratado, puntuación Health Score, CSAT promedio, días para vencimiento.
  - *Factores de Riesgo:* Penalizaciones detalladas calculadas por el motor de salud.
  - *Historial de Tickets:* Casos abiertos, en proceso y resueltos.
  - *Contratos y Renovaciones:* Vencimientos y montos en juego.
  - *Directorio de Contactos:* Personas de enlace (técnico, comercial, decisor).
  - *Encuestas CSAT:* Historial de retroalimentación recibida.

### 4.3. Helpdesk / Mesa de Ayuda y Motor de SLA
- **Ciclo de Estados:** `ABIERTO` ➔ `ASIGNADO` ➔ `EN_PROCESO` ➔ `PENDIENTE` ➔ `RESUELTO` ➔ `CERRADO`.
- **Motor de SLA Dinámico:** Consulta la tabla `configuracion_sla` según la prioridad (`BAJA`, `MEDIA`, `ALTA`, `CRITICA`) y calcula automáticamente las fechas límite de primera respuesta y resolución.
- **Mensajería Dual:** Respuestas visibles para el cliente y notas internas privadas para el equipo técnico.
- **Trazabilidad:** Registro automático en `ticket_historial` ante reasignaciones o cambios de estado.
- **Disparo de CSAT:** Creación automática de encuesta al resolver o cerrar el caso.

### 4.4. Renovaciones y Retención de Contratos
- **Monitoreo de Vencimientos:** Agrupación en rangos de urgencia (<15 días, 16-30 días, 31-60 días).
- **Indicadores Financieros:** Cálculo del ARR total en riesgo de no renovación.
- **Bitácora de Negociación:** Registro de avances comerciales y cambio de estado (`EN_NEGOCIACION`, `RENOVADO`, `NO_RENOVADO`).

### 4.5. Motor de Cálculo de Health Score (`HealthScoreService`)
Calcula un puntaje integral de 0 a 100 evaluando 4 dimensiones:
1. **Tickets Críticos:** Descuento por casos abiertos de alta severidad.
2. **SLA Fuera de Límite:** Penalización por incumplimiento de tiempos de atención.
3. **Calificación CSAT:** Promedio ponderado de encuestas de satisfacción.
4. **Vencimiento Cercano:** Alerta si el contrato vence en menos de 30 días sin renovación acordada.

---

## 5. 🛠️ Tecnologías y Dependencias (`pom.xml`)

| Dependencia | Versión | Propósito |
|---|---|---|
| **Java** | `21 (LTS)` | Entorno de ejecución y lenguaje base |
| **Spring Boot** | `3.3.3` | Framework núcleo de la aplicación |
| **Spring Data JPA** | `3.3.3` | Abstracción de persistencia con Hibernate ORM 6.5 |
| **Spring Security** | `3.3.3` | Autenticación, autorización y protección CSRF |
| **Thymeleaf** | `3.3.3` | Motor de plantillas HTML5 |
| **Thymeleaf Security 6** | `3.1.2` | Integración de directivas de seguridad en vistas |
| **Hibernate Validator** | `8.0.x` | Validación declarativa de modelos y DTOs (JSR-380) |
| **MySQL Connector/J** | `8.3.0` | Driver JDBC oficial para MySQL 8.x |
| **H2 Database** | `2.2.x` | Base de datos relacional en memoria para tests |
| **Springdoc OpenAPI UI** | `2.6.0` | Generador de Swagger UI y especificación OpenAPI 3 |
| **Lombok** | `1.18.x` | Reducción de código repetitivo (getters, constructores, logs) |
| **Spring Boot DevTools** | `3.3.3` | Recarga rápida durante el desarrollo local |
| **Spring Boot Starter Test** | `3.3.3` | JUnit 5, Mockito, AssertJ |
| **Spring Security Test** | `6.3.3` | MockMvc con contexto de seguridad simulado |

---

## 6. ⚙️ Configuración y Conexión a Base de Datos

### Perfil MySQL (`src/main/resources/application-mysql.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/serviclient?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:dcaldas$$}
    driverClassName: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
```

---

## 7. 🚀 Comandos de Utilidad para el Desarrollador

| Acción | Comando Maven |
|---|---|
| **Compilar el proyecto** | `mvn clean compile -DskipTests` |
| **Ejecutar tests automatizados** | `mvn clean test` |
| **Iniciar aplicación (Perfil MySQL)** | `mvn "-Dspring-boot.run.profiles=mysql" spring-boot:run` |
| **Generar sitio Javadoc HTML** | `mvn javadoc:javadoc -DskipTests` (salida en `target/site/apidocs/index.html`) |
| **Generar Javadoc en JAR** | `mvn javadoc:jar -DskipTests` (salida en `target/crm-helpdesk-1.0.0-javadoc.jar`) |
| **Empaquetar Javadoc en ZIP** | `Compress-Archive -Path target\site\apidocs\* -DestinationPath target\javadoc-serviclient-crm.zip -Force` |

### Enlaces Locales de Desarrollo
- **Aplicación Web:** [http://localhost:8080/](http://localhost:8080/)
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Credenciales Demo:** `admin@serviclient.com` / `admin123`
