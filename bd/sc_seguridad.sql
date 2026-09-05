-- =====================================================================
-- Esquema de seguridad
-- Cubre RF-SEG-001 a RF-SEG-009
-- Convención de nombres alineada al diseño base: <entidad>_id
-- =====================================================================

-- Prerrequisito (ejecutar como superusuario, fuera de transacción):
--   CREATE ROLE bienbcb LOGIN PASSWORD '<definir>';
--   CREATE DATABASE bienbcb OWNER bienbcb ENCODING 'UTF8';

CREATE SCHEMA IF NOT EXISTS sc_seguridad AUTHORIZATION bienbcb;


-- ---------------------------------------------------------------------
-- Módulos del sistema (RF-SEG-008: control de acceso por módulo)
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.modulo (
    modulo_id        BIGINT GENERATED ALWAYS AS IDENTITY,
    modulo_padre_id  BIGINT,
    codigo           VARCHAR(30)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    descripcion      VARCHAR(250),
    ruta             VARCHAR(200),
    orden            SMALLINT     NOT NULL DEFAULT 0,
    estado           VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario          VARCHAR(50),
    fecha_hora       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion         VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_modulo PRIMARY KEY (modulo_id),
    CONSTRAINT uk_modulo_codigo UNIQUE (codigo),
    CONSTRAINT fk_modulo_padre
        FOREIGN KEY (modulo_padre_id) REFERENCES sc_seguridad.modulo (modulo_id)
);

CREATE INDEX ix_modulo_padre ON sc_seguridad.modulo (modulo_padre_id);


-- ---------------------------------------------------------------------
-- Permisos (RF-SEG-004)
-- Un permiso es una acción concreta sobre un módulo.
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.permiso (
    permiso_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    modulo_id    BIGINT       NOT NULL,
    codigo       VARCHAR(60)  NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(250),
    estado       VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario      VARCHAR(50),
    fecha_hora   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion     VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_permiso PRIMARY KEY (permiso_id),
    CONSTRAINT uk_permiso_codigo UNIQUE (codigo),
    CONSTRAINT fk_permiso_modulo
        FOREIGN KEY (modulo_id) REFERENCES sc_seguridad.modulo (modulo_id)
);

CREATE INDEX ix_permiso_modulo ON sc_seguridad.permiso (modulo_id);


-- ---------------------------------------------------------------------
-- Roles (RF-SEG-003)
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.rol (
    rol_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo       VARCHAR(30)  NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    descripcion  VARCHAR(250),
    es_sistema   BOOLEAN      NOT NULL DEFAULT FALSE,
    estado       VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario      VARCHAR(50),
    fecha_hora   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion     VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_rol PRIMARY KEY (rol_id),
    CONSTRAINT uk_rol_codigo UNIQUE (codigo)
);


-- ---------------------------------------------------------------------
-- Permisos asignados a cada rol (RF-SEG-004)
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.rol_permiso (
    rol_permiso_id BIGINT GENERATED ALWAYS AS IDENTITY,
    rol_id         BIGINT       NOT NULL,
    permiso_id     BIGINT       NOT NULL,
    estado         VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario        VARCHAR(50),
    fecha_hora     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion       VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_rol_permiso PRIMARY KEY (rol_permiso_id),
    CONSTRAINT uk_rol_permiso UNIQUE (rol_id, permiso_id),
    CONSTRAINT fk_rp_rol
        FOREIGN KEY (rol_id) REFERENCES sc_seguridad.rol (rol_id),
    CONSTRAINT fk_rp_permiso
        FOREIGN KEY (permiso_id) REFERENCES sc_seguridad.permiso (permiso_id)
);

CREATE INDEX ix_rol_permiso_permiso ON sc_seguridad.rol_permiso (permiso_id);


-- ---------------------------------------------------------------------
-- Usuarios (RF-SEG-001, 002, 006, 007)
--
-- password_hash: BCrypt (60 chars) o Argon2id (~95 chars). El algoritmo
-- incrusta el salt en el propio hash, por eso no hay columna 'salt'.
-- Se guarda 'algoritmo' para poder migrar de esquema sin invalidar
-- las credenciales existentes.
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.usuario (
    usuario_id             BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id             BIGINT,
    sucursal_id            BIGINT,
    username               VARCHAR(50)  NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    algoritmo              VARCHAR(20)  NOT NULL DEFAULT 'BCRYPT',
    correo                 VARCHAR(120),
    requiere_cambio_pwd    BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_cambio_pwd       TIMESTAMP(6),
    intentos_fallidos      SMALLINT     NOT NULL DEFAULT 0,
    fecha_ultimo_acceso    TIMESTAMP(6),
    fecha_bloqueo          TIMESTAMP(6),
    motivo_bloqueo         VARCHAR(200),
    estado                 VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario                VARCHAR(50),
    fecha_hora             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion               VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_usuario PRIMARY KEY (usuario_id),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_persona UNIQUE (persona_id),
    CONSTRAINT fk_usuario_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id),
    CONSTRAINT fk_usuario_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sc_administracion.sucursal (sucursal_id),
    CONSTRAINT ck_usuario_intentos
        CHECK (intentos_fallidos >= 0)
);

CREATE INDEX ix_usuario_sucursal ON sc_seguridad.usuario (sucursal_id);
CREATE INDEX ix_usuario_estado   ON sc_seguridad.usuario (estado);


-- ---------------------------------------------------------------------
-- Roles asignados al usuario (RF-SEG-005)
-- Con vigencia, para poder auditar quién tuvo qué rol y cuándo.
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.usuario_rol (
    usuario_rol_id BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id     BIGINT       NOT NULL,
    rol_id         BIGINT       NOT NULL,
    fecha_inicio   DATE         NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin      DATE,
    estado         VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario        VARCHAR(50),
    fecha_hora     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion       VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_rol_id),
    CONSTRAINT uk_usuario_rol UNIQUE (usuario_id, rol_id, fecha_inicio),
    CONSTRAINT fk_ur_usuario
        FOREIGN KEY (usuario_id) REFERENCES sc_seguridad.usuario (usuario_id),
    CONSTRAINT fk_ur_rol
        FOREIGN KEY (rol_id) REFERENCES sc_seguridad.rol (rol_id),
    CONSTRAINT ck_usuario_rol_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX ix_usuario_rol_rol ON sc_seguridad.usuario_rol (rol_id);


-- ---------------------------------------------------------------------
-- Sesiones (RF-SEG-009)
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.sesion (
    sesion_id      BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id     BIGINT       NOT NULL,
    token_hash     VARCHAR(128) NOT NULL,
    fecha_inicio   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin      TIMESTAMP(6),
    fecha_expira   TIMESTAMP(6) NOT NULL,
    motivo_cierre  VARCHAR(30),
    ip_origen      VARCHAR(45),
    user_agent     VARCHAR(250),
    estado         VARCHAR(3)   NOT NULL DEFAULT 'V',
    estacion       VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_sesion PRIMARY KEY (sesion_id),
    CONSTRAINT uk_sesion_token UNIQUE (token_hash),
    CONSTRAINT fk_sesion_usuario
        FOREIGN KEY (usuario_id) REFERENCES sc_seguridad.usuario (usuario_id),
    CONSTRAINT ck_sesion_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE INDEX ix_sesion_usuario ON sc_seguridad.sesion (usuario_id, fecha_inicio DESC);
CREATE INDEX ix_sesion_activa  ON sc_seguridad.sesion (estado, fecha_expira);


-- ---------------------------------------------------------------------
-- Historial de contraseñas (RF-SEG-007)
-- Impide reutilizar las últimas N contraseñas.
-- ---------------------------------------------------------------------
CREATE TABLE sc_seguridad.usuario_pwd_historico (
    pwd_historico_id BIGINT GENERATED ALWAYS AS IDENTITY,
    usuario_id       BIGINT       NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    algoritmo        VARCHAR(20)  NOT NULL,
    fecha_hora       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario_pwd_historico PRIMARY KEY (pwd_historico_id),
    CONSTRAINT fk_pwd_usuario
        FOREIGN KEY (usuario_id) REFERENCES sc_seguridad.usuario (usuario_id)
);

CREATE INDEX ix_pwd_historico_usuario
    ON sc_seguridad.usuario_pwd_historico (usuario_id, fecha_hora DESC);


-- =====================================================================
-- Datos semilla
-- El hash del administrador NO se define aquí. Genéralo desde la
-- aplicación en el primer arranque con BCrypt (coste 12) o Argon2id,
-- o mediante pgcrypto:
--
--   INSERT INTO sc_seguridad.usuario
--       (username, password_hash, algoritmo, requiere_cambio_pwd, estacion)
--   VALUES
--       ('admin', crypt('<clave-temporal>', gen_salt('bf', 12)),
--        'BCRYPT', TRUE, '127.0.0.1');
--
-- requiere_cambio_pwd = TRUE obliga al cambio en el primer ingreso.
-- =====================================================================

INSERT INTO sc_seguridad.rol (codigo, nombre, descripcion, es_sistema, estacion) VALUES
    ('ADMIN',      'Administrador del sistema', 'Acceso total. Rol de sistema, no eliminable.', TRUE,  '127.0.0.1'),
    ('CAJERO',     'Cajero',                    'Operaciones de ventanilla.',                  FALSE, '127.0.0.1'),
    ('OFICIAL',    'Oficial de socios',         'Alta y mantenimiento de socios.',             FALSE, '127.0.0.1'),
    ('CONTADOR',   'Contador',                  'Comprobantes, cierres y reportes contables.', FALSE, '127.0.0.1'),
    ('AUDITOR',    'Auditor',                   'Solo consulta sobre todos los módulos.',      FALSE, '127.0.0.1'),
    ('GERENTE',    'Gerente',                   'Autorización de operaciones y reportes.',     FALSE, '127.0.0.1');

INSERT INTO sc_seguridad.modulo (codigo, nombre, orden, estacion) VALUES
    ('SOCIOS',        'Gestión de socios',           1, '127.0.0.1'),
    ('APORTACIONES',  'Gestión de aportaciones',     2, '127.0.0.1'),
    ('CAPTACIONES',   'Productos de ahorro',         3, '127.0.0.1'),
    ('CONTABILIDAD',  'Contabilidad',                4, '127.0.0.1'),
    ('SEGURIDAD',     'Usuarios, roles y seguridad', 5, '127.0.0.1'),
    ('PARAMETROS',    'Parámetros y configuración',  6, '127.0.0.1');
