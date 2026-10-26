# ServiClient CRM Helpdesk

> Aplicacion web monolitica de gestion de clientes, mesa de ayuda y renovaciones.
> **Java 21 · Spring Boot 3.3.3 · Thymeleaf · Spring Security · Spring Data JPA · MySQL 8**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D.svg)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-MIT%20Academico-lightgrey.svg)](#-licencia)
[![Guava](https://img.shields.io/badge/Google%20Guava-33%2B-yellow.svg)](https://github.com/google/guava)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5%2B-red.svg)](https://poi.apache.org/)

---

## Propósito del Sistema

**ServiClient CRM Helpdesk** es una plataforma web monolítica empresarial orientada a compañías de servicios **B2B** (*Business-to-Business*). Su objetivo central es gestionar de extremo a extremo el ciclo de vida del cliente corporativo: desde el registro multitenancy y onboarding de empresas, hasta el seguimiento de contratos, directorio de contactos, mesa de ayuda multicanal con acuerdos de nivel de servicio (SLA), encuestas de satisfacción (CSAT), motor analítico de salud del cliente (*Health Score*) y control de retención de contratos y ARR (*Annual Recurring Revenue*), complementado con un portal de autoservicio para clientes finales.

### Módulos Principales

| Módulo | Descripción Funcional |
|---|---|
| **Autenticación & Onboarding** | Inicio/cierre de sesión seguro y Wizard de registro en 3 pasos: Empresa ➔ Usuario Administrador ➔ Configuración de SLA, CSAT y Health Score. Incluye control de contraseña temporal obligatoria. |
| **Dashboard Interactivo** | Panel de control ejecutivo en tiempo real con filtros temporales (7d, 30d, 90d, 1y, todo), gráficos interactivos (Chart.js), balance de tickets, satisfacción CSAT y panel de contratos por vencer. |
| **Gestión de Clientes B2B** | Padrón de clientes con búsqueda predictiva, filtros por estado de salud, alta/edición/baja, exportación de reportes a Excel (.xlsx) y **Vista 360°** organizada en pestañas temáticas. |
| **Directorio de Contactos** | Gestión de interlocutores comerciales y técnicos, designación de contacto principal y generación/restablecimiento de credenciales de autoservicio. |
| **Mesa de Ayuda (Helpdesk)** | Gestión del ciclo de vida de tickets con numeración correlativa (`TK-XXXX`), cálculo dinámico de tiempos límite de primera respuesta y resolución (SLA), mensajería dual (pública y notas internas) y reasignación de agentes. |
| **Satisfacción del Cliente (CSAT)** | Generación automática de encuestas al resolver o cerrar tickets de soporte y registro de métricas de satisfacción por cliente. |
| **Panel de Renovaciones & ARR** | Monitoreo de contratos clasificados por urgencia de expiración (<15 días, 16-30 días, 31-60 días, >60 días), cálculo de ARR en riesgo y bitácora de seguimientos comerciales. |
| **Motor de Health Score** | Algoritmo determinista continuo (0 a 100) que califica la salud de cada cuenta evaluando casos críticos, violaciones de SLA, promedio de CSAT y cercanía a la renovación. |
| **Gestión de Equipo & Agentes** | Administración del equipo de atención y supervisión con roles (`ADMIN`, `SUPERVISOR`, `AGENTE`), activación/desactivación y reseteo de claves temporales. |
| **Portal de Autoservicio** | Acceso para clientes externos para creación de incidencias (`/portal/tickets/nuevo`) y seguimiento en línea de sus casos (`/portal/tickets/{id}`). |
| **Documentación OpenAPI 3.0** | Swagger UI interactivo (`/swagger-ui.html`) y especificación en JSON/YAML sincronizada con las anotaciones del controlador. |

---

## Stack Tecnológico

| Capa / Componente | Tecnología | Versión | Propósito en el Proyecto |
|---|---|---|---|
| **Lenguaje Base** | Java (OpenJDK / LTS) | 21 | Plataforma de ejecución moderna con Records, Pattern Matching y Switch Expressions |
| **Framework Principal** | Spring Boot | 3.3.3 | Núcleo de la aplicación web, inyección de dependencias y autoconfiguración |
| **Motor de Plantillas** | Thymeleaf + Extras | 3.1.2 / 3.3.3 | Renderizado del lado del servidor (SSR) con integración de directivas Spring Security 6 |
| **Persistencia & ORM** | Spring Data JPA + Hibernate | 3.3.3 / 6.5 | Mapeo objeto-relacional y consultas optimizadas sobre 26 entidades y 26 repositorios |
| **Seguridad Web** | Spring Security | 6.3.3 | Autenticación basada en sesiones, filtro de contraseñas temporales, roles y CSRF |
| **Base de Datos** | MySQL Server | 8.0+ | Base de datos relacional principal con motor InnoDB y juego de caracteres UTF-8 |
| **Base de Datos de Pruebas** | H2 Database | 2.2.x | Base de datos relacional en memoria para ejecución veloz de pruebas de integración |
| **Documentación OpenAPI** | Springdoc OpenAPI UI | 2.6.0 | Generación automática de Swagger UI (`/swagger-ui.html`) y esquemas OpenAPI 3 |
| **Gestión de Construcción** | Apache Maven | 3.9+ | Ciclo de vida de compilación, empaquetado y generación de Javadoc |
| **Documentación de Código** | Maven Javadoc Plugin | 3.6.3 | Compilación del sitio Javadoc HTML con soporte nativo de Java 21 y UTF-8 |
| **Hojas de Cálculo** | Apache POI (poi-ooxml) | 5.2.5 | Generación en memoria y descarga de reportes Excel (.xlsx) de clientes |
| **Validación Defensiva** | Google Guava | 33.0.0-jre | Validación de invariantes con `Preconditions` (`checkNotNull`, `checkArgument`) |
| **Utilidades de Cadenas** | Apache Commons Lang3 | 3.14.0 | Operaciones auxiliares de validación y formateo de texto |
| **Generación de Código** | Project Lombok | 1.18.30 | Reducción de boilerplate (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`) |
| **Registro de Eventos** | SLF4J con Logback | 1.5.12 | Logging estructurado para auditoría operativa y seguimiento de excepciones |

---

## Arquitectura de Software

El sistema sigue una arquitectura monolítica en capas estrictas (**N-Tier + Spring MVC**) desacoplada mediante interfaces de servicio:

```
com.serviclient.crm
├── config/                                # Configuración global del sistema
│   ├── DataInitializer.java               # Carga inicial de datos semilla en las 26 tablas
│   ├── OpenApiConfig.java                 # Configuración de Swagger UI / OpenAPI 3.0
│   └── security/                          # Seguridad Spring Security
│       ├── CustomUserDetails.java         # Principal autenticado con empresaId y rol
│       ├── CustomUserDetailsService.java  # Carga de credenciales desde UsuarioRepository
│       ├── CambioPasswordObligatorioFilter.java # Interceptor de cambio de clave temporal
│       └── SecurityConfig.java            # Reglas de rutas, sesiones, login y CSRF
├── controller/                            # Controladores Spring MVC (@Controller)
│   ├── AgenteController.java              # Gestión del equipo de soporte
│   ├── AuthController.java                # Login y Wizard de registro en 3 pasos
│   ├── CambioPasswordController.java      # Flujo de cambio de clave obligatoria
│   ├── ClienteController.java             # Padrón de clientes, Vista 360 y exportación
│   ├── DashboardController.java           # Tablero ejecutivo interactivo
│   ├── GlobalControllerAdvice.java        # Inyección de usuario y empresa a vistas
│   ├── PortalTicketController.java        # Creación y consulta de tickets por clientes
│   ├── RenovacionController.java          # Tablero de vencimientos y ARR en riesgo
│   ├── TicketController.java              # Mesa de ayuda, SLA, notas y cierre CSAT
│   └── portalusuario/
│       └── PortalUsuarioController.java   # Autenticación al portal de clientes
├── service/                               # Capa de negocio e interfaces públicas
│   ├── IClienteService.java / ClienteService.java
│   ├── ICuentaPortalService.java / CuentaPortalService.java
│   ├── IDashboardService.java / DashboardService.java
│   ├── IEmpresaService.java / EmpresaService.java
│   ├── IHealthScoreService.java / HealthScoreService.java
│   ├── IPortalTicketService.java / PortalTicketService.java
│   ├── IRenovacionService.java / RenovacionService.java
│   ├── ITicketService.java / TicketService.java
│   └── IUsuarioService.java / UsuarioService.java
├── repository/                            # 26 Repositorios Spring Data JPA
│   ├── ClienteRepository.java / TicketRepository.java / RenovacionRepository.java
│   ├── EmpresaRepository.java / UsuarioRepository.java / CuentaPortalRepository.java
│   └── ... (20 repositorios adicionales)
├── entity/                                # 26 Entidades JPA mapeadas a MySQL
│   ├── Cliente.java / Contacto.java / Ticket.java / Renovacion.java / etc.
│   └── enums/                             # 15 Enumeraciones de dominio (EstadoTicket, etc.)
├── dto/                                   # Objetos de transferencia y validación de formularios
└── ServiClientApplication.java            # Clase de inicio Spring Boot
```

### Patron de capas: Controller -> Interface -> Service -> Repository

Los controllers **nunca inyectan la clase concreta del servicio**, sino su interfaz:

```java
// CORRECTO - Inyeccion por interfaz (bajo acoplamiento)
@Autowired
private IClienteService clienteService;

// INCORRECTO - Inyeccion de la implementacion directa (alto acoplamiento)
// private ClienteService clienteService;
```

Esto aplica el principio de inversion de dependencias (SOLID - D) y permite usar mocks en pruebas sin modificar el controller.

---

## Librerias de Utilidad

### Google Guava

**Uso en el proyecto:** Validacion defensiva de argumentos en la capa de servicio.

```java
// En EmpresaService.java
import com.google.common.base.Preconditions;

Preconditions.checkNotNull(empresa, "La empresa no puede ser nula");
Preconditions.checkArgument(empresa.getId() != null, "El ID de empresa es requerido");
```

Se usa `Preconditions` de Guava para lanzar excepciones descriptivas de forma temprana (fail-fast) antes de ejecutar logica de negocio, evitando `NullPointerException` silenciosas.

---

### Logback (SLF4J)

**Uso en el proyecto:** Logging estructurado en servicios y controladores mediante `@Slf4j` de Lombok.

```java
// En servicios y controladores
@Slf4j
@Service
public class TicketService implements ITicketService {

    public void cerrarTicket(Long ticketId) {
        log.info("Cerrando ticket ID: {}", ticketId);
        log.warn("Ticket {} cerrado sin encuesta CSAT activa", ticketId);
        log.error("Error al cerrar ticket {}: {}", ticketId, e.getMessage());
    }
}
```

Logback es el backend de logging por defecto de Spring Boot. Registra eventos de negocio (creacion de tickets, cambios de estado, errores de SLA) de manera estructurada, sin `System.out.println`.

---

### Apache POI

**Uso en el proyecto:** Generacion dinamica de archivos Excel (.xlsx) para exportacion de datos de clientes.

```java
// En ClienteService.java
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public byte[] exportarClientesExcel(Long empresaId) {
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Clientes");
    // Se construyen filas con nombre, Health Score, estado, servicios activos...
    return baos.toByteArray(); // devuelto como descarga desde el controller
}
```

Permite descargar el listado completo de clientes a un archivo Excel desde `GET /clientes/exportar-excel`.

---

### Apache Commons Lang3

**Uso en el proyecto:** Utilidades genericas de manipulacion de Strings y objetos.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```

Proporciona metodos como `StringUtils.isBlank()`, `StringUtils.capitalize()` y `ObjectUtils.defaultIfNull()` para complementar las utilidades estandar de Java en validaciones y transformaciones dentro de los servicios.

---

## Base de Datos (MySQL)

El proyecto usa **MySQL 8** como base de datos principal (esquema `serviclient`).
El esquema completo esta en `CRM_MESA_AYUDA.sql` e incluye **25 tablas**.

### Tablas del esquema

| # | Tabla | Entidad JPA | Descripcion |
|---|---|---|---|
| 1 | empresas | Empresa | Empresa proveedora del CRM |
| 2 | roles | RolEntity | Roles personalizables por empresa |
| 3 | permisos | Permiso | Permisos granulares por modulo/accion |
| 4 | rol_permisos | (tabla join) | Relacion N:M entre roles y permisos |
| 5 | usuarios | Usuario | Agentes y administradores |
| 6 | servicios | Servicio | Catalogo de productos/servicios |
| 7 | clientes | Cliente | Clientes B2B con Health Score |
| 8 | contactos | Contacto | Contactos por cliente |
| 9 | clientes_servicios | ClienteServicio | Contratos activos de servicios |
| 10 | categorias_tickets | CategoriaTicketEntity | Categorias de tickets por empresa |
| 11 | configuracion_sla | ConfiguracionSla | Tiempos de SLA por prioridad |
| 12 | configuracion_csat | ConfiguracionCsat | Escala y mensaje CSAT |
| 13 | configuracion_health_score | ConfiguracionHealthScore | Pesos y rangos del Health Score |
| 14 | tickets | Ticket | Tickets de soporte (Mesa de ayuda) |
| 15 | ticket_mensajes | TicketMensaje | Conversacion del ticket |
| 16 | ticket_historial | TicketHistorial | Log de eventos del ticket |
| 17 | ticket_adjuntos | TicketAdjunto | Archivos adjuntos |
| 18 | renovaciones | Renovacion | Renovaciones de contrato |
| 19 | renovacion_seguimientos | RenovacionSeguimiento | Historial de seguimientos |
| 20 | encuestas_csat | EncuestaCsat | Encuestas CSAT enviadas |
| 21 | respuestas_csat | RespuestaCsat | Respuestas a encuestas CSAT |
| 22 | encuestas_satisfaccion | EncuestaSatisfaccion | Satisfaccion por ticket |
| 23 | health_score_historial | HealthScoreHistorial | Historico de puntaje de salud |
| 24 | interacciones | Interaccion | Reuniones, llamadas y seguimientos |
| 25 | auditoria | Auditoria | Log de acciones del sistema |

### Configuracion de conexion

```yaml
# application-mysql.yml (valores por defecto)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/serviclient?createDatabaseIfNotExist=true
    username: root
    password:
  jpa:
    hibernate:
      ddl-auto: update
```

Variables de entorno: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`

---

## Requisitos Previos

| Herramienta | Version minima | Descarga |
|---|---|---|
| Java JDK | 21 | https://learn.microsoft.com/es-es/java/openjdk/download |
| Apache Maven | 3.9+ | https://maven.apache.org/download.cgi |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |

---

## Instalación y Ejecución

### Opción 1 - Script Rápido Automatizado (Windows)

El proyecto cuenta con scripts de arranque en la carpeta `bin/` que configuran el entorno Java 21 y Maven, levantan el perfil MySQL y abren automáticamente el navegador en `http://localhost:8080`:

Desde **CMD**:
```cmd
bin\iniciar.bat
```

Desde **PowerShell**:
```powershell
.\bin\iniciar.ps1
```

**Flujo interno del script:**
1. Configura `JAVA_HOME` apuntando al JDK 21 y añade Maven al `PATH`.
2. Si existe el empaquetado `target\crm-helpdesk-1.0.0.jar`, ejecuta:
   ```cmd
   java -jar target\crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
   ```
3. En caso contrario, inicia mediante el plugin de Spring Boot:
   ```cmd
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Opción 2 - Ejecución Directa con Maven

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Opción 3 - Compilación y Ejecución de JAR

```bash
# Compilar y empaquetar
mvn clean package -DskipTests

# Ejecutar el artefacto generado
java -jar target/crm-helpdesk-1.0.0.jar --spring.profiles.active=mysql
```

La aplicación quedará disponible en **http://localhost:8080**

> **Poblado Automático de Datos:** Al iniciar por primera vez, `DataInitializer` detecta si la base de datos se encuentra vacía y carga automáticamente la información semilla de prueba en las 26 tablas del sistema. Los scripts SQL de respaldo se encuentran disponibles en `sql/CRM_MESA_AYUDA.sql` y `sql/CRM_MESA_AYUDA_DATOS_DEMO.sql`.

---

## Credenciales de Demostración

| Correo Electrónico | Contraseña | Rol Asignado |
|---|---|---|
| `admin@serviclient.com` | `admin123` | Administrador General |
| `carlos.ruiz@serviclient.com` | `admin123` | Agente de Soporte |
| `ana.garcia@serviclient.com` | `admin123` | Agente de Soporte |
| `laura.mendoza@serviclient.com` | `admin123` | Supervisor Customer Success |

---

## Documentación de APIs (Swagger / OpenAPI 3.0)

Con la aplicación en ejecución:

| Recurso | URL Local | Descripción |
|---|---|---|
| **Swagger UI Interactivo** | http://localhost:8080/swagger-ui.html | Exploración interactiva y prueba de endpoints |
| **OpenAPI Spec (JSON)** | http://localhost:8080/v3/api-docs | Especificación OpenAPI 3.0 en formato JSON |
| **OpenAPI Spec (YAML)** | http://localhost:8080/v3/api-docs.yaml | Especificación OpenAPI 3.0 en formato YAML |

### Tags de la API Documentados:
- **Autenticación:** Login, logout y wizard de registro en 3 pasos.
- **Dashboard:** Panel interactivo general con métricas, gráficos y KPIs.
- **Clientes:** Padrón, filtros, creación, edición, vista 360 y exportación Excel.
- **Helpdesk / Tickets:** Bandeja de soporte, mensajería, notas privadas y control de SLA.
- **Renovaciones:** Panel de vencimientos, ARR y registro de seguimientos.
- **Agentes:** Administración del equipo de agentes y supervisores.
- **Perfil de Usuario:** Cambio obligatorio de contraseña temporal.
- **Portal de Clientes:** Autoservicio de reporte y consulta externa de incidencias.

---

## Javadoc del Proyecto

El código fuente cuenta con comentarios Javadoc completos en español utilizando `@param`, `@return` y `@throws`:

- **Controllers:** Descripción de flujo MVC, modelos Thymeleaf, parámetros de ruta y redirecciones.
- **Service Interfaces (`IXxxService`) & Implementaciones:** Reglas de negocio, invariantes y precondiciones.
- **Entities & DTOs:** Mapeo relacional, validaciones declarativas Bean Validation (JSR-380) y constructores.
- **Configuración:** OpenAPI, filtros de seguridad y carga de datos semilla.

Para generar el sitio web HTML de Javadoc:
```bash
mvn javadoc:javadoc -DskipTests
# Resultado en: target/site/apidocs/index.html
```

---

## Endpoints Disponibles en el Sistema

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/` | Redirección a `/dashboard` | Autenticado |
| `GET` | `/login` | Pantalla de inicio de sesión | Público |
| `POST` | `/login` | Procesamiento de credenciales (Spring Security) | Público |
| `GET` | `/registro/paso-1` | Wizard de Empresa: datos corporativos | Público |
| `POST` | `/registro/paso-1` | Validación y avance al paso 2 | Público |
| `GET` | `/registro/paso-2` | Wizard de Administrador: credenciales de acceso | Público |
| `POST` | `/registro/paso-2` | Validación y avance al paso 3 | Público |
| `GET` | `/registro/paso-3` | Wizard de Configuración: umbrales SLA, CSAT y salud | Público |
| `POST` | `/registro/paso-3` | Persistencia atómica de la empresa y administrador | Público |
| `GET` | `/dashboard` | Tablero ejecutivo interactivo con KPIs y gráficos | Autenticado |
| `GET` | `/cambiar-password` | Redirección o acceso al cambio de contraseña temporal | Autenticado |
| `POST` | `/cambiar-password` | Procesar cambio de clave obligatoria | Autenticado |
| `GET` | `/clientes` | Listado paginado de clientes con filtros y KPIs de salud | Autenticado |
| `GET` | `/clientes/nuevo` | Formulario de alta de cliente B2B | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes` | Persistir nuevo cliente y credenciales de portal | `ADMIN` / `SUPERVISOR` |
| `GET` | `/clientes/{id}` | Vista 360° del cliente con tabs temáticas | Autenticado |
| `GET` | `/clientes/{id}/editar` | Formulario de edición de cliente | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes/{id}/editar` | Actualizar datos del cliente | `ADMIN` / `SUPERVISOR` |
| `POST` | `/clientes/{id}/eliminar` | Eliminación de cliente del sistema | `ADMIN` |
| `GET` | `/clientes/exportar` | Descarga de clientes en formato Excel (.xlsx) | Autenticado |
| `POST` | `/clientes/{clienteId}/contactos` | Agregar nuevo contacto al cliente | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/editar` | Modificar datos del contacto | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/principal` | Designar contacto principal y generar credenciales | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/desactivar` | Desactivar acceso del contacto | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/reactivar` | Reactivar contacto y regenerar clave temporal | `ADMIN` |
| `POST` | `/clientes/{clienteId}/contactos/{contactoId}/portal/restablecer` | Restablecer contraseña temporal del portal | `ADMIN` |
| `GET` | `/tickets` | Bandeja de tickets con KPIs de SLA y filtros combinados | Autenticado |
| `GET` | `/tickets/nuevo` | Formulario para registrar nuevo ticket de soporte | Autenticado |
| `POST` | `/tickets` | Crear ticket con cálculo automático de SLA | Autenticado |
| `GET` | `/tickets/{id}` | Detalle del ticket con conversación, notas y tiempos | Autenticado |
| `POST` | `/tickets/{id}/mensajes` | Enviar respuesta visible o nota interna privada | Autenticado |
| `POST` | `/tickets/{id}/asignar` | Reasignar agente responsable | `ADMIN` / `SUPERVISOR` |
| `POST` | `/tickets/{id}/cambiar-estado` | Modificar estado y/o severidad del ticket | Autenticado |
| `POST` | `/tickets/{id}/cerrar` | Cerrar ticket y disparar encuesta CSAT si aplica | Autenticado |
| `GET` | `/renovaciones` | Tablero de contratos próximos a vencer y ARR en riesgo | Autenticado |
| `POST` | `/renovaciones/seguimiento` | Registrar avance comercial de renovación | Autenticado |
| `GET` | `/agentes` | Listado y gestión del equipo de agentes y supervisores | `ADMIN` |
| `POST` | `/agentes` | Crear agente con generación de clave temporal | `ADMIN` |
| `POST` | `/agentes/{id}/editar` | Modificar nombre, email o rol del agente | `ADMIN` |
| `POST` | `/agentes/{id}/estado` | Alternar estado activo / inactivo de un agente | `ADMIN` |
| `POST` | `/agentes/{id}/restablecer-password` | Generar y entregar nueva clave temporal al agente | `ADMIN` |
| `GET` | `/portal/tickets/nuevo` | Formulario público para creación de tickets por clientes | Público |
| `GET` | `/portal/tickets/{id}` | Consulta del estado y conversación de un ticket | Público / Contacto |
| `GET` | `/portalusuario/login` | Pantalla de inicio de sesión en portal de usuarios | Público |
| `GET` | `/portalusuario/cambiar-password` | Cambio de contraseña de usuario de portal | Público |
| `GET` | `/swagger-ui.html` | Interfaz interactiva de documentación Swagger | Autenticado |
| `GET` | `/v3/api-docs` | Especificación de esquema OpenAPI 3 en formato JSON | Autenticado |

---

## Pruebas

```bash
mvn clean test
```

| Clase de Test | Tipo | Cobertura |
|---|---|---|
| ServiClientApplicationTests | Integracion | Contexto Spring Boot + DataInitializer |
| SecurityAndAuthControllerTest | MockMvc | Login, Wizard 3 pasos, autorizacion |
| JpaRepositoriesTest | @DataJpaTest | Repositorios, filtros JPA, consultas |
| HealthScoreServiceTest | Unitario (Mockito) | Calculo de puntaje y factores de riesgo |
| TicketServiceTest | Unitario (Mockito) | SLA, mensajeria, cierre con CSAT y auditoria |

---

## Modelo de Dominio

```
Empresa --< RolEntity --< Permiso (N:M via rol_permisos)
Empresa --< Usuario (rol_id -> RolEntity)
Empresa --< Cliente --< Contacto
                   --< ClienteServicio --> Servicio
                   --< Ticket --< TicketMensaje
                              --< TicketHistorial
                              --< TicketAdjunto
                              --- EncuestaCsat --< RespuestaCsat
                   --< Renovacion --< RenovacionSeguimiento
                   --< Interaccion
                   --< HealthScoreHistorial
Empresa --< CategoriaTicketEntity
Empresa --< ConfiguracionSla
Empresa --< ConfiguracionCsat
Empresa --< ConfiguracionHealthScore
Empresa --< Auditoria
Empresa --< Notificacion
```

### Enums del dominio (15 enumeraciones)

| Enum | Valores |
|---|---|
| Rol | ADMIN, AGENTE, SUPERVISOR |
| EstadoCliente | SALUDABLE, OBSERVACION, EN_RIESGO |
| EstadoSaludCliente | SALUDABLE, EN_OBSERVACION, EN_RIESGO, CRITICO |
| EstadoTicket | ABIERTO, EN_PROCESO, PENDIENTE, RESUELTO, CERRADO |
| PrioridadTicket | BAJA, MEDIA, ALTA, CRITICA |
| CategoriaTicket | INFRAESTRUCTURA, SOFTWARE, FACTURACION, ADMINISTRACION, SOPORTE_TECNICO |
| EstadoRenovacion | PENDIENTE, EN_NEGOCIACION, RENOVADO, NO_RENOVADO, CANCELADO |
| EstadoEncuestaCsat | PENDIENTE, ENVIADA, RESPONDIDA, VENCIDA |
| EstadoServicioCliente | ACTIVO, INACTIVO, SUSPENDIDO, CANCELADO |
| EstadoGeneral | ACTIVO, INACTIVO |
| TipoMensajeTicket | CLIENTE, AGENTE, NOTA_INTERNA, SISTEMA |
| TipoInteraccion | LLAMADA, EMAIL, REUNION, SEGUIMIENTO, OTRO |
| TipoNotificacion | TICKET_ASIGNADO, TICKET_VENCIDO, RENOVACION_PROXIMA, CLIENTE_EN_RIESGO, CSAT_RECIBIDO |
| CanalOrigen | PORTAL_WEB, EMAIL, TELEFONO, CHAT, API |
| AccionAuditoria | LOGIN, LOGOUT, CREAR, EDITAR, ELIMINAR, VER |

---

## Principios Aplicados

- **SOLID** - Interfaces de servicio (`IXxxService`) aplican SRP y DIP; cada capa tiene una sola razon de cambio
- **DRY** - Layout maestro Thymeleaf reutilizable en todas las vistas
- **Bajo acoplamiento** - Controllers dependen de interfaces, no de implementaciones concretas
- **Fail-fast** - Guava `Preconditions` valida argumentos antes de ejecutar logica de negocio
- **Javadoc** - Documentacion en codigo para controllers, interfaces, services y configuracion
- **OpenAPI 3.0** - Swagger UI auto-generado desde las anotaciones del codigo
- **Seguridad** - CSRF habilitado, contrasenas con BCrypt, rutas protegidas por rol

---

## Licencia

Proyecto academico - Libre uso educativo (MIT).
