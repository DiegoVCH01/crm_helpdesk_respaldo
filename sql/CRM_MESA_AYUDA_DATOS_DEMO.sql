-- =====================================================
-- SERVICLIENT CRM & HELPDESK - DATOS DE PRUEBA / DEMO
-- Coherentes con los indicadores y widgets del Dashboard
-- =====================================================

USE serviclient;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Empresa Principal
INSERT INTO empresas (id, nombre_comercial, razon_social, ruc, industria, tamano_empresa, pais, ciudad, direccion, sitio_web, zona_horaria, estado, created_at, updated_at)
VALUES (1, 'ServiClient', 'ServiClient Cloud Solutions S.A.C.', '20601234567', 'Tecnología & SaaS', '50-200', 'Perú', 'Lima', 'Av. Javier Prado Este 4200, San Isidro', 'https://serviclient.com', 'America/Lima', 'ACTIVA', NOW(), NOW())
ON DUPLICATE KEY UPDATE nombre_comercial = VALUES(nombre_comercial);

-- 2. Roles
INSERT INTO roles
    (id, nombre, descripcion, estado, created_at, updated_at)
VALUES
    (1, 'Administrador',
     'Acceso total y configuración del sistema',
     'ACTIVO', NOW(), NOW()),

    (2, 'Agente de Soporte',
     'Atención y resolución de tickets e incidencias',
     'ACTIVO', NOW(), NOW()),

    (3, 'Supervisor Customer Success',
     'Gestión de salud de clientes, CSAT y renovaciones',
     'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    descripcion = VALUES(descripcion);

-- 3. Usuarios Demo (Contraseña común: admin123 -> $2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92)
INSERT INTO usuarios (id, empresa_id, rol_id, nombre, apellido, email, password_hash, telefono, estado, created_at, updated_at)
VALUES
(1, 1, 1, 'Juan', 'Pérez', 'admin@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 987 654 321', 'ACTIVO', NOW(), NOW()),
(2, 1, 2, 'Carlos', 'Ruiz', 'carlos.ruiz@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 981 111 222', 'ACTIVO', NOW(), NOW()),
(3, 1, 2, 'Ana', 'Silva', 'ana.silva@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 982 333 444', 'ACTIVO', NOW(), NOW()),
(4, 1, 3, 'Laura', 'Mendoza', 'laura.mendoza@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 983 555 666', 'ACTIVO', NOW(), NOW()),
(5, 1, 2, 'María', 'López', 'maria.lopez@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 984 777 888', 'ACTIVO', NOW(), NOW()),
(6, 1, 2, 'Javier', 'Ramos', 'javier.ramos@serviclient.com', '$2a$10$M922F8Iuclyofh3F4UE52Omx3bJVY06uzHZoOBdAFLsCmcwYHRG92', '+51 985 999 000', 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE email = VALUES(email), password_hash = VALUES(password_hash), estado = VALUES(estado);

-- 4. Servicios
INSERT INTO servicios (id, empresa_id, nombre, descripcion, codigo, estado, created_at, updated_at)
VALUES
(1, 1, 'ERP Cloud Enterprise', 'Sistema integral de planificación de recursos empresariales en la nube', 'SRV-ERP', 'ACTIVO', NOW(), NOW()),
(2, 1, 'Helpdesk Omni-channel', 'Plataforma de soporte y atención a usuarios multicanal', 'SRV-HD', 'ACTIVO', NOW(), NOW()),
(3, 1, 'CRM Ventas Pro', 'Gestión de relaciones comerciales y pipeline de ventas', 'SRV-CRM', 'ACTIVO', NOW(), NOW()),
(4, 1, 'Analítica Avanzada & IA', 'Módulo de analítica predictiva y dashboards ejecutivos', 'SRV-AI', 'ACTIVO', NOW(), NOW()),
(5, 1, 'Soporte Premium 24/7', 'Mesa de ayuda dedicada con SLA prioritario garantizado', 'SRV-SP24', 'ACTIVO', NOW(), NOW()),
(6, 1, 'Módulo Analytics', 'Tableros BI y visualización de indicadores en tiempo real', 'SRV-ANL', 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- 5. Categorías de Tickets
INSERT INTO categorias_ticket (id, empresa_id, nombre, descripcion, estado, created_at, updated_at)
VALUES
(1, 1, 'Soporte Técnico', 'Problemas y consultas de operación técnica', 'ACTIVO', NOW(), NOW()),
(2, 1, 'Facturación & Cobranzas', 'Consultas de pagos, recibos y contratos', 'ACTIVO', NOW(), NOW()),
(3, 1, 'Capacitación & Onboarding', 'Entrenamiento e inducción para nuevos usuarios', 'ACTIVO', NOW(), NOW()),
(4, 1, 'Solicitud de Feature', 'Requerimientos y mejoras solicitadas', 'ACTIVO', NOW(), NOW()),
(5, 1, 'Incidencia Crítica', 'Interrupción de servicios críticos', 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- 6. Configuraciones de SLA
INSERT INTO configuracion_sla (id, empresa_id, prioridad, tiempo_respuesta_minutos, tiempo_resolucion_minutos, estado, created_at, updated_at)
VALUES
(1, 1, 'CRITICA', 30, 240, 'ACTIVO', NOW(), NOW()),
(2, 1, 'ALTA', 60, 480, 'ACTIVO', NOW(), NOW()),
(3, 1, 'MEDIA', 120, 1440, 'ACTIVO', NOW(), NOW()),
(4, 1, 'BAJA', 240, 2880, 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

-- 7. Configuración CSAT y Health Score
INSERT INTO configuracion_csat (id, empresa_id, activo, escala_min, escala_max, pregunta, mensaje_agradecimiento, created_at, updated_at)
VALUES (1, 1, TRUE, 1, 5, '¿Qué tan satisfecho estás con la atención recibida?', 'Gracias por compartir tu opinión con ServiClient.', NOW(), NOW())
ON DUPLICATE KEY UPDATE pregunta = VALUES(pregunta);

INSERT INTO configuracion_health_score (id, empresa_id, peso_tickets, peso_csat, peso_actividad, peso_renovacion, rango_saludable_min, rango_observacion_min, estado, created_at, updated_at)
VALUES (1, 1, 30.00, 30.00, 15.00, 25.00, 80, 50, 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

-- 8. Clientes del Dashboard
INSERT INTO clientes (id, empresa_id, responsable_id, nombre_comercial, razon_social, ruc, industria, tamano_empresa, pais, ciudad, direccion, sitio_web, estado, created_at, updated_at)
VALUES
(1, 1, 3, 'TechCorp Industries', 'TechCorp Industries S.A.C.', '20554433221', 'Tecnología & SaaS', '200-500', 'Perú', 'Lima', 'Av. Canaval y Moreyra 480, San Isidro', 'https://techcorp.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 6 MONTH), NOW()),
(2, 1, 2, 'Global Logistics SA', 'Global Logistics del Perú S.A.', '20443322110', 'Logística & Transporte', '100-250', 'Perú', 'Callao', 'Av. Elmer Faucett 2050, Callao', 'https://globallogistics.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 6 MONTH), NOW()),
(3, 1, 5, 'Grupo Financiero Sur', 'Grupo Financiero del Sur S.A.A.', '20112233445', 'Banca & Finanzas', '1000+', 'Perú', 'Lima', 'Av. Las Begonias 450, San Isidro', 'https://grupofinancierosur.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 6 MONTH), NOW()),
(4, 1, 2, 'NovaTech Industries', 'NovaTech Innovations S.A.C.', '20998877665', 'Manufactura Inteligente', '250-500', 'Perú', 'Lima', 'Av. Argentina 3080, Cercado de Lima', 'https://novatech.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 5 MONTH), NOW()),
(5, 1, 4, 'InnovaSoft', 'InnovaSoft Consulting S.A.C.', '20334455667', 'Software & Consultoría', '50-100', 'Perú', 'Lima', 'Calle Dean Valdivia 148, San Isidro', 'https://innovasoft.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 12 MONTH), NOW()),
(6, 1, 1, 'FinTech Solutions', 'FinTech Solutions Latam S.A.C.', '20778899001', 'Fintech & Pagos', '50-200', 'Perú', 'Lima', 'Av. Pardo y Aliaga 640, San Isidro', 'https://fintechsolutions.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
(7, 1, 6, 'CloudNet Corp', 'CloudNet Services del Perú S.A.', '20889900112', 'Telecomunicaciones & Cloud', '500-1000', 'Perú', 'Lima', 'Av. Javier Prado Este 2500, San Borja', 'https://cloudnetcorp.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 14 MONTH), NOW()),
(8, 1, 2, 'Retail Express Perú', 'Retail Express Perú S.A.', '20123456789', 'Retail & E-commerce', '100-250', 'Perú', 'Lima', 'Av. Larco 743, Miraflores', 'https://retailexpress.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 9 MONTH), NOW()),
(9, 1, 4, 'Banco Metropolitano', 'Banco Metropolitano S.A.', '20665544332', 'Banca Múltiple', '1000+', 'Perú', 'Lima', 'Av. República de Panamá 3055, San Isidro', 'https://bancometropolitano.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 11 MONTH), NOW()),
(10, 1, 3, 'Minera Los Andes', 'Compañía Minera Los Andes S.A.C.', '20556677889', 'Minería & Energía', '500-1000', 'Perú', 'Arequipa', 'Av. Cayma 600, Arequipa', 'https://minerala.pe', 'ACTIVO', DATE_SUB(NOW(), INTERVAL 4 MONTH), NOW())
ON DUPLICATE KEY UPDATE nombre_comercial = VALUES(nombre_comercial);

-- 9. Contactos
INSERT INTO contactos (id, cliente_id, nombre, apellido, cargo, email, telefono, es_contacto_principal, estado, created_at, updated_at)
VALUES
(1, 1, 'Roberto', 'Gómez', 'CTO', 'roberto.gomez@techcorp.pe', '+51 999 111 222', TRUE, 'ACTIVO', NOW(), NOW()),
(2, 2, 'Mariana', 'Vargas', 'Gerente de Operaciones', 'm.vargas@globallogistics.pe', '+51 988 222 333', TRUE, 'ACTIVO', NOW(), NOW()),
(3, 3, 'Luis', 'Morales', 'Director de Analítica', 'l.morales@grupofinancierosur.pe', '+51 977 333 444', TRUE, 'ACTIVO', NOW(), NOW()),
(4, 4, 'Andrés', 'Castillo', 'Jefe de Producción TI', 'a.castillo@novatech.pe', '+51 966 444 555', TRUE, 'ACTIVO', NOW(), NOW()),
(5, 5, 'Sofía', 'Benítez', 'Head of Digital', 's.benitez@innovasoft.pe', '+51 955 666 777', TRUE, 'ACTIVO', NOW(), NOW()),
(6, 6, 'Diego', 'Vega', 'Chief Product Officer', 'd.vega@fintechsolutions.pe', '+51 944 555 666', TRUE, 'ACTIVO', NOW(), NOW()),
(7, 7, 'Patricia', 'Rivas', 'Líder de Soporte TI', 'p.rivas@cloudnetcorp.pe', '+51 933 444 555', TRUE, 'ACTIVO', NOW(), NOW()),
(8, 8, 'Julio', 'Navarro', 'Jefe de Mesa de Ayuda', 'j.navarro@retailexpress.pe', '+51 922 333 444', TRUE, 'ACTIVO', NOW(), NOW()),
(9, 9, 'Fernando', 'Castro', 'Gerente de Infraestructura', 'f.castro@bancometropolitano.pe', '+51 911 222 333', TRUE, 'ACTIVO', NOW(), NOW()),
(10, 10, 'Lucía', 'Salazar', 'Jefa de Sistemas', 'lucia.salazar@minerala.pe', '+51 900 111 222', TRUE, 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 10. Health Score Historial (Salud de clientes)
INSERT INTO health_score_historial (id, cliente_id, puntaje, estado, puntaje_tickets, puntaje_csat, puntaje_actividad, puntaje_renovacion, fecha_calculo, created_at)
VALUES
(1, 1, 45.00, 'EN_RIESGO', 12.00, 14.00, 7.00, 12.00, NOW(), NOW()),
(2, 2, 85.00, 'SALUDABLE', 26.00, 26.00, 13.00, 20.00, NOW(), NOW()),
(3, 3, 95.00, 'SALUDABLE', 29.00, 29.00, 14.00, 23.00, NOW(), NOW()),
(4, 4, 65.00, 'EN_OBSERVACION', 18.00, 20.00, 10.00, 17.00, NOW(), NOW()),
(5, 5, 90.00, 'SALUDABLE', 28.00, 27.00, 13.00, 22.00, NOW(), NOW()),
(6, 6, 88.00, 'SALUDABLE', 27.00, 26.00, 13.00, 22.00, NOW(), NOW()),
(7, 7, 82.00, 'SALUDABLE', 25.00, 24.00, 13.00, 20.00, NOW(), NOW()),
(8, 8, 64.00, 'EN_OBSERVACION', 16.00, 19.00, 10.00, 19.00, NOW(), NOW()),
(9, 9, 42.00, 'EN_RIESGO', 10.00, 12.00, 6.00, 14.00, NOW(), NOW()),
(10, 10, 92.00, 'SALUDABLE', 28.00, 28.00, 14.00, 22.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE puntaje = VALUES(puntaje);

-- 11. Clientes Servicios (Contratos)
INSERT INTO cliente_servicios (id, cliente_id, servicio_id, fecha_inicio, fecha_fin, estado, created_at, updated_at)
VALUES
(1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 11 MONTH), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'ACTIVO', NOW(), NOW()),
(2, 2, 5, DATE_SUB(CURDATE(), INTERVAL 8 MONTH), DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'ACTIVO', NOW(), NOW()),
(3, 3, 6, DATE_SUB(CURDATE(), INTERVAL 6 MONTH), DATE_ADD(CURDATE(), INTERVAL 32 DAY), 'ACTIVO', NOW(), NOW()),
(4, 4, 1, DATE_SUB(CURDATE(), INTERVAL 5 MONTH), DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'ACTIVO', NOW(), NOW()),
(5, 5, 3, DATE_SUB(CURDATE(), INTERVAL 12 MONTH), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'ACTIVO', NOW(), NOW()),
(6, 6, 1, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 'ACTIVO', NOW(), NOW()),
(7, 7, 2, DATE_SUB(CURDATE(), INTERVAL 14 MONTH), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'ACTIVO', NOW(), NOW()),
(8, 8, 2, DATE_SUB(CURDATE(), INTERVAL 9 MONTH), DATE_ADD(CURDATE(), INTERVAL 24 DAY), 'ACTIVO', NOW(), NOW()),
(9, 9, 3, DATE_SUB(CURDATE(), INTERVAL 11 MONTH), DATE_ADD(CURDATE(), INTERVAL 12 DAY), 'ACTIVO', NOW(), NOW()),
(10, 10, 4, DATE_SUB(CURDATE(), INTERVAL 4 MONTH), DATE_ADD(CURDATE(), INTERVAL 8 MONTH), 'ACTIVO', NOW(), NOW())
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

-- 12. Renovaciones de Contratos
INSERT INTO renovaciones (id, empresa_id, cliente_id, cliente_servicio_id, responsable_id, fecha_vencimiento, fecha_renovacion, dias_alerta, estado, notas, created_at, updated_at)
VALUES
(1, 1, 1, 1, 3, DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 30, 'EN_NEGOCIACION', 'Negociación en curso: Se presentó propuesta con 10% de descuento y SLA 24/7.', NOW(), NOW()),
(2, 1, 9, 9, 4, DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 30, 'EN_NEGOCIACION', 'Cuenta en riesgo: reunión urgente agendada con Gerencia de TI.', NOW(), NOW()),
(3, 1, 8, 8, 2, DATE_ADD(CURDATE(), INTERVAL 24 DAY), NULL, 30, 'PENDIENTE', 'Enviar propuesta de renovación de Helpdesk antes de fin de mes.', NOW(), NOW()),
(4, 1, 2, 2, 2, DATE_ADD(CURDATE(), INTERVAL 25 DAY), NULL, 30, 'PENDIENTE', 'Contrato de Soporte Premium 24/7 listo para formalización.', NOW(), NOW()),
(5, 1, 3, 3, 5, DATE_ADD(CURDATE(), INTERVAL 32 DAY), NULL, 60, 'PENDIENTE', 'Evaluando expansión de licencias para el Módulo Analytics.', NOW(), NOW()),
(6, 1, 4, 4, 2, DATE_ADD(CURDATE(), INTERVAL 45 DAY), NULL, 60, 'PENDIENTE', 'Coordinar demo de nuevas funciones del ERP.', NOW(), NOW()),
(7, 1, 10, 10, 3, DATE_ADD(CURDATE(), INTERVAL 180 DAY), NULL, 60, 'PENDIENTE', 'Contrato vigente de Analítica Avanzada & IA.', NOW(), NOW()),
(8, 1, 6, 6, 1, DATE_ADD(CURDATE(), INTERVAL 210 DAY), NULL, 60, 'PENDIENTE', 'Cliente nuevo en onboarding; renovación programada para el próximo año.', NOW(), NOW()),
(9, 1, 5, 5, 4, DATE_ADD(CURDATE(), INTERVAL 360 DAY), DATE_SUB(CURDATE(), INTERVAL 5 DAY), 30, 'RENOVADO', 'Renovado por 12 meses adicionales con expansión de 30 puestos.', NOW(), NOW()),
(10, 1, 7, 7, 6, DATE_ADD(CURDATE(), INTERVAL 360 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 30, 'RENOVADO', 'Renovado satisfactoriamente con SLA Diamante.', NOW(), NOW())
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

-- 13. Tickets de Helpdesk
INSERT INTO tickets (id, empresa_id, cliente_id, contacto_id, categoria_id, agente_id, numero_ticket, asunto, descripcion, prioridad, estado, sla_respuesta_limite, sla_resolucion_limite, fecha_primera_respuesta, fecha_resolucion, created_at, updated_at)
VALUES
(1, 1, 9, 9, 5, 2, 'TK-4029', 'Falla en módulo de Facturación Electrónica SUNAT', 'No se pueden emitir comprobantes desde las 08:30 AM. Error HTTP 500 al comunicarse con el web service de la SUNAT.', 'CRITICA', 'ABIERTO', DATE_ADD(NOW(), INTERVAL 25 MINUTE), DATE_ADD(NOW(), INTERVAL 3 HOUR), NULL, NULL, DATE_SUB(NOW(), INTERVAL 35 MINUTE), NOW()),
(2, 1, 4, 4, 5, 6, 'TK-4030', 'Nuevo ticket crítico registrado por NovaTech Industries', 'Interrupción parcial de la sincronización de inventarios y pasarelas de pago.', 'CRITICA', 'ABIERTO', DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NOW()),
(3, 1, 8, 8, 1, 3, 'TK-4028', 'Lentitud en la generación de reportes consolidados', 'Los reportes mensuales de inventario tardan más de 10 minutos en exportar a Excel.', 'ALTA', 'EN_PROCESO', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 3 HOUR), NOW()),
(4, 1, 1, 1, 3, 4, 'TK-4025', 'Configuración de nuevo usuario administrador', 'Requerimos habilitar credenciales de acceso para el nuevo analista financiero.', 'MEDIA', 'RESUELTO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 23 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(5, 1, 7, 7, 1, 6, 'TK-4018', 'Ticket TK-4018 marcado como resuelto', 'Ajuste en la configuración de webhooks de monitoreo de enlaces de red.', 'MEDIA', 'RESUELTO', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 47 HOUR), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(6, 1, 5, 5, 1, 2, 'TK-4009', 'Encuesta CSAT recibida: 5/5', 'Sincronización de contactos e historial de ventas completada sin interrupciones.', 'ALTA', 'RESUELTO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 71 HOUR), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(7, 1, 6, 6, 4, 5, 'TK-4012', 'Consulta sobre configuración de roles personalizados', 'Deseamos restringir el acceso al módulo de conciliación para el perfil Operador.', 'BAJA', 'RESUELTO', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 95 HOUR), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
(8, 1, 2, 2, 2, 3, 'TK-4005', 'Actualización de razón social en comprobantes fiscales', 'Se solicita actualizar la dirección fiscal en la plantilla de facturas emitidas.', 'MEDIA', 'RESUELTO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 119 HOUR), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(9, 1, 1, 1, 5, 3, 'TK-4032', 'Falla intermitente en sincronización de stock con almacén', 'Se detectan descalces de inventario en el turno noche.', 'CRITICA', 'PENDIENTE', DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW()),
(10, 1, 9, 9, 1, 6, 'TK-4035', 'Latencia en consultas de base de datos bancaria', 'Tiempos de respuesta superiores a 2.5 segundos en módulo de transferencias.', 'ALTA', 'ABIERTO', DATE_ADD(NOW(), INTERVAL 50 MINUTE), DATE_ADD(NOW(), INTERVAL 5 HOUR), NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW()),
(11, 1, 10, 10, 4, 4, 'TK-4038', 'Habilitación de tablero de control de sensores IoT', 'Solicitud para integrar telemetría de camiones autónomos en el módulo Analytics.', 'BAJA', 'ABIERTO', DATE_ADD(NOW(), INTERVAL 4 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 HOUR), NOW())
ON DUPLICATE KEY UPDATE numero_ticket = VALUES(numero_ticket);

-- 14. Encuestas CSAT
INSERT INTO encuestas_csat (id, empresa_id, cliente_id, contacto_id, ticket_id, estado, fecha_envio, fecha_respuesta, created_at, updated_at)
VALUES
(1, 1, 1, 1, 4, 'RESPONDIDA', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 18 HOUR), NOW(), NOW()),
(2, 1, 7, 7, 5, 'RESPONDIDA', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
(3, 1, 5, 5, 6, 'RESPONDIDA', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), NOW()),
(4, 1, 6, 6, 7, 'RESPONDIDA', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NOW()),
(5, 1, 2, 2, 8, 'RESPONDIDA', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE estado = VALUES(estado);

INSERT INTO respuestas_csat (id, encuesta_id, puntuacion, comentario, created_at)
VALUES
(1, 1, 5, 'Excelente soporte, todo quedó habilitado en minutos.', NOW()),
(2, 2, 5, 'Muy rápida la atención del equipo de Javier.', NOW()),
(3, 3, 5, 'Excelente atención de Carlos.', NOW()),
(4, 4, 4, 'Buena guía y documentación provista.', NOW()),
(5, 5, 4, 'Atención oportuna.', NOW())
ON DUPLICATE KEY UPDATE puntuacion = VALUES(puntuacion);

-- 15. Auditoría
INSERT INTO auditoria (id, empresa_id, usuario_id, accion, entidad, registro_id, direccion_ip, created_at)
VALUES
(1, 1, 6, 'INSERT', 'TICKETS', 2, '127.0.0.1', DATE_SUB(NOW(), INTERVAL 28 MINUTE)),
(2, 1, 2, 'INSERT', 'ENCUESTAS_CSAT', 6, '127.0.0.1', DATE_SUB(NOW(), INTERVAL 135 MINUTE)),
(3, 1, 1, 'INSERT', 'CLIENTES', 6, '127.0.0.1', DATE_SUB(NOW(), INTERVAL 230 MINUTE)),
(4, 1, 3, 'UPDATE', 'RENOVACIONES', 1, '127.0.0.1', DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(5, 1, 6, 'UPDATE', 'TICKETS', 5, '127.0.0.1', DATE_SUB(NOW(), INTERVAL 22 HOUR))
ON DUPLICATE KEY UPDATE entidad = VALUES(entidad);

SET FOREIGN_KEY_CHECKS = 1;
