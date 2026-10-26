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

### 2.2. Diagrama de Capas y Flujo Operativo

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CAPA DE PRESENTACIÓN                           │
│  - Vistas Thymeleaf (HTML5 / Bootstrap 5 / Vanilla JS / Chart.js)       │
│  - Portal de Clientes y Portal de Usuarios (/portal/**, /portalusuario) │
│  - Swagger UI (/swagger-ui.html) & OpenAPI Endpoints (/v3/api-docs)     │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE CONTROLADORES                           │
│  - AuthController: Login, Logout, Wizard de Registro en 3 pasos         │
│  - ClienteController: Listado paginado, Alta, Vista 360°, Contactos, Xls│
│  - TicketController: Mesa de ayuda, Conversaciones, SLA, CSAT, Cierre   │
│  - RenovacionController: Panel de vencimientos y Seguimientos           │
│  - DashboardController: Tablero ejecutivo interactivo con KPIs          │
│  - AgenteController: Gestión del equipo de agentes y supervisores       │
│  - CambioPasswordController: Cambio obligatorio de clave temporal       │
│  - PortalTicketController & PortalUsuarioController: Autoservicio      │
│  - GlobalControllerAdvice: Contexto de usuario, empresa y menú activo   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE SEGURIDAD (Spring Security)                 │
│  - SecurityConfig: Reglas de autorización, BCrypt (fuerza 12), CSRF    │
│  - CustomUserDetailsService & CustomUserDetails: Autenticación JDBC/JPA │
│  - CambioPasswordObligatorioFilter: Control de primer inicio de sesión  │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       CAPA DE SERVICIOS / NEGOCIO                       │
│  - IClienteService / ClienteService: CRUD, métricas, Vista 360°, Excel  │
│  - ITicketService / TicketService: Ciclo de vida, cálculo SLA, CSAT    │
│  - IRenovacionService / RenovacionService: Detección vencimientos, ARR  │
│  - IHealthScoreService / HealthScoreService: Motor de scoring (0-100)   │
│  - IDashboardService / DashboardService: KPIs y agregación analítica    │
│  - IUsuarioService / UsuarioService: Agentes, roles y credenciales      │
│  - IEmpresaService / EmpresaService: Onboarding con validación Guava    │
│  - ICuentaPortalService & IPortalTicketService: Autoservicio cliente   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    CAPA DE ACCESO A DATOS (Spring Data JPA)             │
│  - 26 Interfaces Repository (Spring Data JPA / Hibernate ORM 6.5)       │
│  - Consultas JPQL optimizadas, filtros dinámicos y paginación en BD     │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     CAPA DE BASE DE DATOS Y PERSISTENCIA                │
│  - MySQL 8.x: 26 Tablas normalizadas con claves foráneas e índices      │
│  - H2 Database: Base de datos relacional en memoria para testing        │
│  - DataInitializer: Poblado automático de datos semilla (seed data)     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 📦 Estructura del Proyecto

```
com.serviclient.crm
├── config/
│   ├── DataInitializer.java               # Inicialización automática de datos semilla en las 26 tablas
│   ├── OpenApiConfig.java                 # Configuración de OpenAPI 3.0 / Swagger UI
│   └── security/
│       ├── CustomUserDetails.java         # Adaptador UserDetails con empresaId y rol
│       ├── CustomUserDetailsService.java  # Carga de credenciales por email
│       ├── CambioPasswordObligatorioFilter.java # Redirección obligatoria a cambio de clave temporal
│       └── SecurityConfig.java            # Configuración de endpoints públicos, sesiones y CSRF
├── controller/
│   ├── AgenteController.java              # Administración del equipo de soporte y supervisión
│   ├── AuthController.java                # Autenticación y Wizard de registro en 3 pasos
│   ├── CambioPasswordController.java      # Actualización de contraseña tras primer acceso
│   ├── ClienteController.java             # Padrón de clientes, Vista 360° y exportación Excel
│   ├── DashboardController.java           # Tablero analítico ejecutivo interactivo
│   ├── GlobalControllerAdvice.java        # Inyección de usuario, empresa y URI a plantillas
│   ├── PortalTicketController.java        # Formulario público y seguimiento de tickets por clientes
│   ├── RenovacionController.java          # Tablero de vencimientos, ARR y seguimientos comerciales
│   ├── TicketController.java              # Mesa de ayuda, mensajería, notas privadas y SLA
│   └── portalusuario/
│       └── PortalUsuarioController.java   # Login y cambio de clave para usuarios del portal
├── dto/
│   ├── AgenteDto.java                     # Formulario de alta/edición de agentes
│   ├── ClienteDto.java                    # Formulario de alta/edición de clientes B2B
│   ├── CredencialTemporalDto.java         # Entrega de usuario y contraseña temporal
│   ├── DashboardDto.java                  # Modelo de datos agregados para el dashboard
│   ├── PortalTicketDto.java               # Captura de datos en el portal de autoservicio
│   ├── RegistroEmpresaDto.java            # Datos del Wizard de registro en 3 pasos
│   └── TicketMensajeDto.java              # Mensajería, notas internas y seguimientos
├── entity/                                # 26 Entidades JPA mapeadas
│   ├── Cliente.java / Contacto.java / ClienteServicio.java / CuentaPortal.java
│   ├── Empresa.java / RolEntity.java / Permiso.java / Usuario.java
│   ├── Servicio.java / CategoriaTicketEntity.java
│   ├── Ticket.java / TicketMensaje.java / TicketHistorial.java / TicketAdjunto.java
│   ├── EncuestaCsat.java / RespuestaCsat.java / EncuestaSatisfaccion.java
│   ├── Renovacion.java / RenovacionSeguimiento.java / Interaccion.java
│   ├── ConfiguracionSla.java / ConfiguracionCsat.java / ConfiguracionHealthScore.java
│   ├── HealthScoreHistorial.java / Auditoria.java / Notificacion.java
│   └── enums/                             # 15 Enums tipados (EstadoTicket, PrioridadTicket, Rol, etc.)
├── repository/                            # 26 Repositorios Spring Data JPA
│   ├── ClienteRepository.java / TicketRepository.java / RenovacionRepository.java
│   ├── UsuarioRepository.java / EmpresaRepository.java / CuentaPortalRepository.java
│   └── ... (20 interfaces repository adicionales)
└── service/                               # Lógica de negocio e interfaces públicas
    ├── IClienteService.java / ClienteService.java
    ├── ICuentaPortalService.java / CuentaPortalService.java
    ├── IDashboardService.java / DashboardService.java
    ├── IEmpresaService.java / EmpresaService.java
    ├── IHealthScoreService.java / HealthScoreService.java
    ├── IPortalTicketService.java / PortalTicketService.java
    ├── IRenovacionService.java / RenovacionService.java
    ├── ITicketService.java / TicketService.java
    └── IUsuarioService.java / UsuarioService.java
```

---

## 4. 🧩 Módulos Funcionales y Funcionamiento Detallado

### 4.1. Autenticación, Seguridad y Onboarding de Empresa
- **Control de Acceso:** Autenticación por formulario mediante Spring Security con codificación BCrypt (fuerza 12).
- **Wizard de Registro Multitenant en 3 Pasos (`@SessionAttributes`):**
  1. *Paso 1:* Razón social, RUC, industria, tamaño y país de la empresa proveedora.
  2. *Paso 2:* Creación del usuario administrador principal.
  3. *Paso 3:* Configuración inicial de acuerdos SLA (tiempos en horas por prioridad), umbral de CSAT y ponderación del motor de salud. Se persiste de forma atómica en base de datos.
- **Filtro de Contraseña Temporal (`CambioPasswordObligatorioFilter`):** Intercepta peticiones HTTP de usuarios con el flag `debeCambiarPassword = true` forzando la redirección a `/cambiar-password` hasta que definan una nueva contraseña.

### 4.2. Padrón de Clientes B2B y Vista 360°
- **Listado Paginado con Búsqueda:** Búsqueda textual por nombre comercial o razón social y filtrado por estado (`SALUDABLE`, `OBSERVACION`, `EN_RIESGO`).
- **Exportación a Excel:** Generación en memoria con Apache POI (`/clientes/exportar`) aplicando los filtros vigentes.
- **Ficha 360° Modular por Pestañas:**
  - *Resumen de KPIs:* CSAT promedio, tickets abiertos y críticos, días para vencimiento y desglose de penalizaciones del Health Score.
  - *Directorio de Contactos:* Personas de enlace (técnico, decisor, comercial). Permite alta, edición, designación de contacto principal, desactivación/reactivación y regeneración de credenciales para el portal.
  - *Historial de Tickets:* Casos asociados al cliente clasificados por estado y prioridad.
  - *Contratos y Renovaciones:* Servicios contratados, fechas de vencimiento y monto ARR.
  - *Encuestas CSAT:* Historial de calificaciones y comentarios recibidos.
  - *Actividad e Interacciones:* Bitácora histórica de llamadas, reuniones y notas.

### 4.3. Helpdesk / Mesa de Ayuda y Motor de SLA
- **Ciclo de Vida de Incidencias:** Estados tipados: `NUEVO` ➔ `ASIGNADO` ➔ `EN_PROCESO` ➔ `ESPERANDO_CLIENTE` ➔ `RESUELTO` ➔ `CERRADO`.
- **Cálculo Automático de SLA:** Al crear un ticket, el sistema consulta `configuracion_sla` de la empresa y computa:
  - `fechaLimiteRespuesta`: Tiempo máximo para el primer contacto de un agente.
  - `fechaLimiteResolucion`: Tiempo máximo para resolver la incidencia.
- **Mensajería Dual:** El hilo del ticket admite mensajes externos (visibles para el cliente) y notas internas (visibles únicamente para el equipo de soporte).
- **Trazabilidad Automática:** Toda reasignación, cambio de prioridad o estado genera un registro inmutable en `ticket_historial`.
- **Disparo de Encuesta CSAT:** Al resolver o cerrar el ticket, se dispara automáticamente la creación de una encuesta de satisfacción vinculada al caso.

### 4.4. Renovaciones de Contratos y Control de Retención (ARR)
- **Matriz de Urgencia:** Clasificación automática de contratos en ventanas temporales (<15 días, 16-30 días, 31-60 días y >60 días).
- **Indicador Financiero de Riesgo:** Sumatoria consolidada del ARR (Annual Recurring Revenue) en riesgo de no renovación.
- **Seguimientos Comerciales:** Registro de interacciones comerciales con actualización de estado (`EN_NEGOCIACION`, `RENOVADO`, `NO_RENOVADO`, `CANCELADO`).

### 4.5. Motor Algorítmico de Health Score (`HealthScoreService`)
Calcula en tiempo real un índice de 0 a 100 para cada cliente evaluando 4 dimensiones clave:
1. **Tickets Críticos Abiertos:** Penalización sustancial por incidencias de severidad crítica no resueltas.
2. **SLA Fuera de Límite:** Descuento progresivo por tickets cuyo límite de resolución o primera respuesta haya vencido.
3. **Desempeño CSAT:** Ponderación positiva o negativa según el promedio histórico de calificaciones del cliente.
4. **Proximidad de Vencimiento:** Alerta de riesgo si el contrato expira en menos de 30 días sin confirmación de renovación.

### 4.6. Gestión del Equipo de Agentes (`AgenteController` & `UsuarioService`)
- Listado y control del personal de atención y supervisión de la empresa.
- Creación de agentes mediante modales interactivos con asignación de roles (`AGENTE`, `SUPERVISOR`) y generación de contraseña temporal.
- Edición de información, alternancia de estado activo/inactivo y regeneración inmediata de contraseñas olvidadas.

### 4.7. Portal de Autoservicio de Clientes (`PortalTicketController` & `PortalUsuarioController`)
- **Reporte Externo:** Formulario público (`/portal/tickets/nuevo`) para que los clientes finales registren incidencias sin acceder al CRM administrativo.
- **Seguimiento en Línea:** Consulta del estado, tiempos y conversación del caso (`/portal/tickets/{id}`).
- **Portal de Usuarios:** Acceso autenticado independiente (`/portalusuario/login`) con flujo de actualización de contraseña.

### 4.8. Tablero General Ejecutivo (`DashboardController` & `DashboardService`)
- Métricas consolidadas en tiempo real: total de clientes, distribución de salud operativa, tickets abiertos y fuera de SLA, CSAT promedio y ARR en riesgo.
- Filtros dinámicos por rango temporal (`7d`, `30d`, `90d`, `1y`, `todo`).
- Visualización gráfica mediante Chart.js para tendencias y distribución de incidencias.

---

## 5. 🛠️ Tecnologías y Dependencias (`pom.xml`)

| Dependencia | Versión | Propósito en la Arquitectura |
|---|---|---|
| **Java (LTS)** | `21` | Lenguaje de programación base con soporte para Records y Pattern Matching |
| **Spring Boot Starter Web** | `3.3.3` | Servidor embebido Tomcat, controladores Spring MVC y manejo HTTP |
| **Spring Boot Starter Data JPA** | `3.3.3` | Abstracción de persistencia con Hibernate ORM 6.5 |
| **Spring Boot Starter Security** | `3.3.3` | Autenticación, control de sesiones, filtros y protección CSRF |
| **Spring Boot Starter Thymeleaf** | `3.3.3` | Motor de renderizado del lado del servidor (SSR) |
| **Thymeleaf Extras Spring Security 6** | `3.1.2` | Integración de directivas `sec:authorize` y roles en plantillas |
| **MySQL Connector/J** | `8.3.0` | Driver JDBC oficial para conexión a MySQL 8.x |
| **H2 Database** | `2.2.x` | Base de datos relacional en memoria para tests automatizados |
| **Springdoc OpenAPI UI** | `2.6.0` | Generador automático de Swagger UI y especificación OpenAPI 3 |
| **Maven Javadoc Plugin** | `3.6.3` | Generación y validación del sitio de documentación Javadoc en HTML |
| **Apache POI (poi-ooxml)** | `5.2.5` | Creación dinámica y exportación de archivos Excel (.xlsx) |
| **Google Guava** | `33.0.0-jre` | Validación defensiva fail-fast con `Preconditions` en la capa de servicios |
| **Apache Commons Lang3** | `3.14.0` | Utilidades auxiliares de cadenas y objetos |
| **Project Lombok** | `1.18.30` | Reducción de código repetitivo (getters, constructores, logging `@Slf4j`) |
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
