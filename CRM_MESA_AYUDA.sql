-- =====================================================
-- BASE DE DATOS
-- ServiClient
-- =====================================================

CREATE DATABASE IF NOT EXISTS serviclient
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE serviclient;


-- =====================================================
-- 1. EMPRESAS
-- Cada registro representa una empresa SaaS
-- que utiliza ServiClient.
-- =====================================================

CREATE TABLE empresas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre_comercial VARCHAR(150) NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    industria VARCHAR(100),
    tamano_empresa VARCHAR(50),
    pais VARCHAR(100),
    ciudad VARCHAR(100),
    direccion VARCHAR(255),
    sitio_web VARCHAR(255),
    logo_url VARCHAR(500),
    zona_horaria VARCHAR(100) DEFAULT 'America/Lima',
    estado ENUM('ACTIVA', 'INACTIVA') 
        NOT NULL DEFAULT 'ACTIVA',
    created_at TIMESTAMP 
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP 
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_empresa_ruc UNIQUE (ruc)
);


-- =====================================================
-- 2. ROLES
-- Roles propios de cada empresa.
-- =====================================================

CREATE TABLE roles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_rol_nombre
        UNIQUE (nombre)
);


-- =====================================================
-- 3. USUARIOS
-- Personas que utilizan ServiClient.
-- =====================================================

CREATE TABLE usuarios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    rol_id BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30),
    avatar_url VARCHAR(500),
    debe_cambiar_password BOOLEAN NOT NULL DEFAULT FALSE,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO')
        NOT NULL DEFAULT 'ACTIVO',
    ultimo_acceso DATETIME NULL,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_email_empresa
        UNIQUE (empresa_id, email),
    CONSTRAINT fk_usuarios_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_usuarios_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 4. CLIENTES
-- Empresas que son clientes de una empresa SaaS.
-- =====================================================

CREATE TABLE clientes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    responsable_id BIGINT UNSIGNED NULL,
    nombre_comercial VARCHAR(150) NOT NULL,
    razon_social VARCHAR(200),
    ruc VARCHAR(20),
    industria VARCHAR(100),
    tamano_empresa VARCHAR(50),
    pais VARCHAR(100),
    ciudad VARCHAR(100),
    direccion VARCHAR(255),
    sitio_web VARCHAR(255),
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    -- Un mismo RUC puede ser cliente de distintas
    -- empresas SaaS, pero no debe repetirse dentro
    -- de la misma empresa.
    CONSTRAINT uq_cliente_ruc_empresa
        UNIQUE (empresa_id, ruc),
    CONSTRAINT fk_clientes_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_clientes_responsable
        FOREIGN KEY (responsable_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =====================================================
-- 5. CONTACTOS
-- Personas pertenecientes a las empresas clientes.
-- =====================================================

CREATE TABLE contactos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    cargo VARCHAR(120),
    email VARCHAR(180),
    telefono VARCHAR(30),
    es_contacto_principal BOOLEAN
        NOT NULL DEFAULT FALSE,
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_contacto_email_cliente
        UNIQUE (cliente_id, email),
    CONSTRAINT fk_contactos_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 6. SERVICIOS
-- Productos o servicios ofrecidos por cada empresa SaaS.
-- =====================================================

CREATE TABLE servicios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    codigo VARCHAR(50),
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_servicio_codigo_empresa
        UNIQUE (empresa_id, codigo),

    CONSTRAINT fk_servicios_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 7. CLIENTE_SERVICIOS
-- Servicios contratados por cada cliente.
-- =====================================================

CREATE TABLE cliente_servicios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT UNSIGNED NOT NULL,
    servicio_id BIGINT UNSIGNED NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NULL,
    estado ENUM(
        'ACTIVO',
        'SUSPENDIDO',
        'FINALIZADO'
    ) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cliente_servicio
        UNIQUE (cliente_id, servicio_id),
    CONSTRAINT fk_cliente_servicio_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_cliente_servicio_servicio
        FOREIGN KEY (servicio_id)
        REFERENCES servicios(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- =====================================================
-- 8. PERMISOS
-- Catálogo de acciones disponibles en ServiClient.
-- =====================================================

CREATE TABLE permisos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    modulo VARCHAR(80) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    CONSTRAINT uq_permiso_nombre
        UNIQUE (nombre)
);

-- =====================================================
-- 9. ROL_PERMISOS
-- Relación entre roles y permisos.
-- =====================================================

CREATE TABLE rol_permisos (
    rol_id BIGINT UNSIGNED NOT NULL,
    permiso_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permisos_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_rol_permisos_permiso
        FOREIGN KEY (permiso_id)
        REFERENCES permisos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================================
-- 10. INTERACCIONES
-- Registra actividades relevantes relacionadas
-- con un cliente.
-- Se utilizará principalmente en Cliente 360°.
-- =====================================================

CREATE TABLE interacciones (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    tipo ENUM(
        'LLAMADA',
        'REUNION',
        'CORREO',
        'SEGUIMIENTO',
        'NOTA',
        'OTRO'
    ) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_interaccion DATETIME NOT NULL,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interacciones_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_interacciones_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_interaccion_cliente_fecha
        (cliente_id, fecha_interaccion),
    INDEX idx_interaccion_usuario
        (usuario_id)
);

-- =====================================================
-- 11. CATEGORIAS_TICKET
-- Categorías utilizadas para clasificar los tickets.
-- =====================================================

CREATE TABLE categorias_ticket (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_categoria_empresa
        UNIQUE (empresa_id, nombre),
    CONSTRAINT fk_categoria_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 12. CONFIGURACION_SLA
-- Tiempos máximos de respuesta y resolución
-- según la prioridad del ticket.
-- =====================================================

CREATE TABLE configuracion_sla (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    prioridad ENUM(
        'CRITICA',
        'ALTA',
        'MEDIA',
        'BAJA'
    ) NOT NULL,
    tiempo_respuesta_minutos INT UNSIGNED NOT NULL,
    tiempo_resolucion_minutos INT UNSIGNED NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_sla_empresa_prioridad
        UNIQUE (empresa_id, prioridad),
    CONSTRAINT fk_sla_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 13. CONFIGURACION_CSAT
-- Configuración de las encuestas de satisfacción.
-- Esta tabla se agrega para cubrir la configuración
-- de CSAT definida en ServiClient.
-- =====================================================

CREATE TABLE configuracion_csat (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    activo BOOLEAN
        NOT NULL DEFAULT TRUE,
    escala_min TINYINT UNSIGNED
        NOT NULL DEFAULT 1,
    escala_max TINYINT UNSIGNED
        NOT NULL DEFAULT 5,
    pregunta VARCHAR(255)
        NOT NULL DEFAULT
        '¿Qué tan satisfecho estás con la atención recibida?',
    mensaje_agradecimiento VARCHAR(255)
        DEFAULT 'Gracias por compartir tu opinión.',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_csat_empresa
        UNIQUE (empresa_id),
    CONSTRAINT fk_config_csat_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_csat_escala
        CHECK (escala_min < escala_max)
);


-- =====================================================
-- 14. CONFIGURACION_HEALTH_SCORE
-- Pesos y rangos utilizados para calcular
-- el estado de salud de los clientes.
-- =====================================================

CREATE TABLE configuracion_health_score (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    peso_tickets DECIMAL(5,2)
        NOT NULL DEFAULT 30.00,
    peso_csat DECIMAL(5,2)
        NOT NULL DEFAULT 30.00,
    peso_actividad DECIMAL(5,2)
        NOT NULL DEFAULT 15.00,
    peso_renovacion DECIMAL(5,2)
        NOT NULL DEFAULT 25.00,
    rango_saludable_min TINYINT UNSIGNED
        NOT NULL DEFAULT 80,
    rango_observacion_min TINYINT UNSIGNED
        NOT NULL DEFAULT 50,
    estado ENUM('ACTIVO', 'INACTIVO')
        NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_health_empresa
        UNIQUE (empresa_id),
    CONSTRAINT fk_health_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_health_rangos
        CHECK (
            rango_saludable_min > rango_observacion_min
            AND rango_saludable_min <= 100
            AND rango_observacion_min <= 100
        ),
	CONSTRAINT chk_health_pesos
		CHECK (
			peso_tickets >= 0
			AND peso_csat >= 0
			AND peso_actividad >= 0
			AND peso_renovacion >= 0
			AND (
				peso_tickets
				+ peso_csat
				+ peso_actividad
				+ peso_renovacion
			) = 100.00
		)
);


-- =====================================================
-- 15. TICKETS
-- Solicitudes o incidencias registradas
-- dentro de la Mesa de Ayuda.
-- =====================================================

CREATE TABLE tickets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    cliente_id BIGINT UNSIGNED NOT NULL,
    contacto_id BIGINT UNSIGNED NULL,
    categoria_id BIGINT UNSIGNED NOT NULL,
    agente_id BIGINT UNSIGNED NULL,
    numero_ticket VARCHAR(30) NOT NULL,
    asunto VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    prioridad ENUM(
        'CRITICA',
        'ALTA',
        'MEDIA',
        'BAJA'
    ) NOT NULL DEFAULT 'MEDIA',
    estado ENUM(
        'ABIERTO',
        'EN_PROCESO',
        'PENDIENTE',
        'RESUELTO',
        'CERRADO'
    ) NOT NULL DEFAULT 'ABIERTO',
    sla_respuesta_limite DATETIME NULL,
    sla_resolucion_limite DATETIME NULL,
    fecha_primera_respuesta DATETIME NULL,
    fecha_resolucion DATETIME NULL,
    fecha_cierre DATETIME NULL,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_ticket_numero_empresa
        UNIQUE (empresa_id, numero_ticket),
    CONSTRAINT fk_ticket_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias_ticket(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_agente
        FOREIGN KEY (agente_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_ticket_empresa_estado
        (empresa_id, estado),
    INDEX idx_ticket_cliente
        (cliente_id),
    INDEX idx_ticket_agente
        (agente_id),
    INDEX idx_ticket_prioridad
        (prioridad),
    INDEX idx_ticket_created_at
        (created_at)
);


-- =====================================================
-- 16. TICKET_MENSAJES
-- Conversaciones entre agentes y contactos
-- relacionados con un ticket.
-- =====================================================

CREATE TABLE ticket_mensajes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    contacto_id BIGINT UNSIGNED NULL,
    tipo ENUM(
        'AGENTE',
        'CLIENTE',
        'SISTEMA'
    ) NOT NULL,
    mensaje TEXT NOT NULL,
    es_interno BOOLEAN
        NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensaje_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_mensaje_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_mensaje_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_mensaje_ticket_fecha
        (ticket_id, created_at)
);


-- =====================================================
-- 17. TICKET_HISTORIAL
-- Historial de cambios realizados sobre tickets.
-- =====================================================

CREATE TABLE ticket_historial (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    tipo_evento VARCHAR(80) NOT NULL,
    valor_anterior VARCHAR(255) NULL,
    valor_nuevo VARCHAR(255) NULL,
    descripcion VARCHAR(500),
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_historial_ticket_fecha
        (ticket_id, created_at)
);


-- =====================================================
-- 18. TICKET_ADJUNTOS
-- Información de archivos asociados a tickets.
-- El archivo físico NO se guarda en MySQL.
-- =====================================================

CREATE TABLE ticket_adjuntos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT UNSIGNED NOT NULL,
    mensaje_id BIGINT UNSIGNED NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    url_archivo VARCHAR(500) NOT NULL,
    tipo_archivo VARCHAR(100),
    tamano BIGINT UNSIGNED,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adjunto_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_adjunto_mensaje
        FOREIGN KEY (mensaje_id)
        REFERENCES ticket_mensajes(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =====================================================
-- 19. RENOVACIONES
-- Procesos de renovación de los servicios
-- contratados por los clientes.
-- =====================================================

CREATE TABLE renovaciones (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    cliente_id BIGINT UNSIGNED NOT NULL,
    cliente_servicio_id BIGINT UNSIGNED NOT NULL,
    responsable_id BIGINT UNSIGNED NULL,
    fecha_vencimiento DATE NOT NULL,
    dias_alerta INT UNSIGNED
        NOT NULL DEFAULT 30,
    estado ENUM(
        'PENDIENTE',
        'EN_NEGOCIACION',
        'RENOVADO',
        'NO_RENOVADO'
    ) NOT NULL DEFAULT 'PENDIENTE',
    fecha_renovacion DATE NULL,
    notas TEXT,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_renovacion_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_renovacion_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_renovacion_cliente_servicio
        FOREIGN KEY (cliente_servicio_id)
        REFERENCES cliente_servicios(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_renovacion_responsable
        FOREIGN KEY (responsable_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_renovacion_empresa_estado
        (empresa_id, estado),
    INDEX idx_renovacion_vencimiento
        (fecha_vencimiento),
    INDEX idx_renovacion_cliente
        (cliente_id)
);


-- =====================================================
-- 20. RENOVACION_SEGUIMIENTOS
-- Acciones realizadas durante una renovación.
-- =====================================================

CREATE TABLE renovacion_seguimientos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    renovacion_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,
    fecha DATETIME NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'EN_NEGOCIACION',
        'RENOVADO',
        'NO_RENOVADO'
    ) NOT NULL,
    comentario TEXT,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seguimiento_renovacion
        FOREIGN KEY (renovacion_id)
        REFERENCES renovaciones(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_seguimiento_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    INDEX idx_seguimiento_renovacion_fecha
        (renovacion_id, fecha)
);


-- =====================================================
-- 21. ENCUESTAS_CSAT
-- Encuestas de satisfacción relacionadas
-- con la atención de tickets.
-- =====================================================

CREATE TABLE encuestas_csat (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    cliente_id BIGINT UNSIGNED NOT NULL,
    contacto_id BIGINT UNSIGNED NULL,
    ticket_id BIGINT UNSIGNED NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'RESPONDIDA',
        'EXPIRADA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio DATETIME NULL,
    fecha_respuesta DATETIME NULL,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_encuesta_ticket
        UNIQUE (ticket_id),
    CONSTRAINT fk_encuesta_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_encuesta_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_encuesta_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_encuesta_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_encuesta_cliente
        (cliente_id),
    INDEX idx_encuesta_estado
        (estado)
);


-- =====================================================
-- 22. RESPUESTAS_CSAT
-- Respuesta proporcionada por el cliente
-- a una encuesta CSAT.
-- =====================================================

CREATE TABLE respuestas_csat (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    encuesta_id BIGINT UNSIGNED NOT NULL,
    puntuacion TINYINT UNSIGNED NOT NULL,
    comentario TEXT,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_respuesta_encuesta
        UNIQUE (encuesta_id),
    CONSTRAINT fk_respuesta_encuesta
        FOREIGN KEY (encuesta_id)
        REFERENCES encuestas_csat(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =====================================================
-- 23. HEALTH_SCORE_HISTORIAL
-- Guarda la evolución histórica del Health Score
-- de cada cliente.
-- =====================================================

CREATE TABLE health_score_historial (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT UNSIGNED NOT NULL,
    puntaje DECIMAL(5,2) NOT NULL,
    estado ENUM(
        'SALUDABLE',
        'EN_OBSERVACION',
        'EN_RIESGO'
    ) NOT NULL,
    puntaje_tickets DECIMAL(5,2) NULL,
    puntaje_csat DECIMAL(5,2) NULL,
    puntaje_actividad DECIMAL(5,2) NULL,
    puntaje_renovacion DECIMAL(5,2) NULL,
    fecha_calculo DATETIME
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_historial_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_health_puntaje
        CHECK (puntaje >= 0 AND puntaje <= 100),
    INDEX idx_health_cliente_fecha
        (cliente_id, fecha_calculo)
);


-- =====================================================
-- 24. NOTIFICACIONES
-- Alertas dirigidas a los usuarios.
-- =====================================================

CREATE TABLE notificaciones (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NOT NULL,
    tipo ENUM(
        'CLIENTE_EN_RIESGO',
        'RENOVACION_PROXIMA',
        'SLA_PROXIMO_VENCER',
        'SLA_INCUMPLIDO',
        'CSAT_BAJO',
        'TICKET_ASIGNADO',
        'TICKET_ACTUALIZADO'
    ) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    -- Indica a qué registro pertenece la alerta.
    -- Ejemplo:
    -- entidad_tipo = 'TICKET'
    -- entidad_id = 25
    entidad_tipo VARCHAR(50) NULL,
    entidad_id BIGINT UNSIGNED NULL,
    leida BOOLEAN
        NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notificacion_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_notificacion_usuario_leida
        (usuario_id, leida),
    INDEX idx_notificacion_empresa
        (empresa_id)
);

-- =====================================================
-- 25. Auditoria general
-- saber las operaciones 
-- =====================================================

CREATE TABLE auditoria (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    empresa_id BIGINT UNSIGNED NOT NULL,
    usuario_id BIGINT UNSIGNED NULL,

    accion ENUM(
        'LOGIN',
        'LOGOUT',
        'INSERT',
        'UPDATE',
        'DELETE'
    ) NOT NULL,

    entidad VARCHAR(100) NOT NULL,
    registro_id BIGINT UNSIGNED NULL,

    datos_anteriores JSON NULL,
    datos_nuevos JSON NULL,

    direccion_ip VARCHAR(45) NULL,
    created_at TIMESTAMP
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_empresa
        FOREIGN KEY (empresa_id)
        REFERENCES empresas(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_auditoria_empresa_fecha
        (empresa_id, created_at),

    INDEX idx_auditoria_usuario_fecha
        (usuario_id, created_at),

    INDEX idx_auditoria_entidad_registro
        (entidad, registro_id)
);


CREATE INDEX idx_clientes_empresa_estado
ON clientes (empresa_id, estado);

CREATE INDEX idx_renovacion_empresa_fecha_estado
ON renovaciones (
    empresa_id,
    fecha_vencimiento,
    estado
);

CREATE INDEX idx_encuesta_empresa_estado
ON encuestas_csat (
    empresa_id,
    estado
);