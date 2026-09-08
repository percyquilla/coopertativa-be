-- Rehecho de sc_seguridad (CONTEXTO-BD.md §5.2 / §7 Paso 3), a partir de
-- bd/sc_seguridad.sql. Transforma el puente creado en V1__init.sql
-- (usuario con password/salt/rol de texto libre) a la forma real:
-- password_hash (BCrypt), y roles resueltos vía usuario_rol/rol.

CREATE EXTENSION IF NOT EXISTS pgcrypto; -- solo para el hash de la semilla; el resto del hashing lo hace la app (BCrypt en Java)

-- La única fila del puente es el admin semilla con hash SHA-256; se
-- reemplaza por una semilla BCrypt más abajo.
DELETE FROM sc_seguridad.usuario;

ALTER TABLE sc_seguridad.usuario
    DROP CONSTRAINT ck_usuario_rol,
    DROP COLUMN password,
    DROP COLUMN salt,
    DROP COLUMN rol,
    DROP COLUMN activo,
    ADD COLUMN sucursal_id         BIGINT,
    ADD COLUMN password_hash       VARCHAR(255) NOT NULL,
    ADD COLUMN algoritmo           VARCHAR(20)  NOT NULL DEFAULT 'BCRYPT',
    ADD COLUMN correo              VARCHAR(120),
    ADD COLUMN requiere_cambio_pwd BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN fecha_cambio_pwd    TIMESTAMP(6),
    ADD COLUMN intentos_fallidos   SMALLINT     NOT NULL DEFAULT 0,
    ADD COLUMN fecha_ultimo_acceso TIMESTAMP(6),
    ADD COLUMN fecha_bloqueo       TIMESTAMP(6),
    ADD COLUMN motivo_bloqueo      VARCHAR(200),
    ADD COLUMN estado              VARCHAR(3)   NOT NULL DEFAULT 'V',
    ADD COLUMN usuario             VARCHAR(50),
    ADD COLUMN estacion            VARCHAR(30)  NOT NULL DEFAULT 'init';

ALTER TABLE sc_seguridad.usuario
    RENAME CONSTRAINT uq_usuario_username TO uk_usuario_username;
ALTER TABLE sc_seguridad.usuario
    RENAME CONSTRAINT uq_usuario_persona TO uk_usuario_persona;

ALTER TABLE sc_seguridad.usuario
    ADD CONSTRAINT fk_usuario_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sc_administracion.sucursal (sucursal_id),
    ADD CONSTRAINT ck_usuario_intentos
        CHECK (intentos_fallidos >= 0);

CREATE INDEX ix_usuario_sucursal ON sc_seguridad.usuario (sucursal_id);
CREATE INDEX ix_usuario_estado   ON sc_seguridad.usuario (estado);

-- ============================================================
-- Módulos, permisos, roles (RF-SEG-003, 004, 008)
-- ============================================================

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

-- ============================================================
-- Roles por usuario, con vigencia (RF-SEG-005)
-- ============================================================

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

-- ============================================================
-- Sesiones (RF-SEG-009) e historial de contraseñas (RF-SEG-007)
-- Tablas creadas ahora; sin uso desde Java todavía (JWT sigue siendo
-- stateless) — es trabajo pendiente fuera del alcance de este paso.
-- ============================================================

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

-- ============================================================
-- Datos semilla
-- ============================================================

INSERT INTO sc_seguridad.rol (codigo, nombre, descripcion, es_sistema, estacion) VALUES
    ('ADMIN',      'Administrador del sistema', 'Acceso total. Rol de sistema, no eliminable.', TRUE,  'init'),
    ('CAJERO',     'Cajero',                    'Operaciones de ventanilla.',                  FALSE, 'init'),
    ('OFICIAL',    'Oficial de socios',         'Alta y mantenimiento de socios.',             FALSE, 'init'),
    ('CONTADOR',   'Contador',                  'Comprobantes, cierres y reportes contables.', FALSE, 'init'),
    ('AUDITOR',    'Auditor',                   'Solo consulta sobre todos los módulos.',      FALSE, 'init'),
    ('GERENTE',    'Gerente',                   'Autorización de operaciones y reportes.',     FALSE, 'init');

INSERT INTO sc_seguridad.modulo (codigo, nombre, orden, estacion) VALUES
    ('SOCIOS',        'Gestión de socios',           1, 'init'),
    ('APORTACIONES',  'Gestión de aportaciones',     2, 'init'),
    ('CAPTACIONES',   'Productos de ahorro',         3, 'init'),
    ('CONTABILIDAD',  'Contabilidad',                4, 'init'),
    ('SEGURIDAD',     'Usuarios, roles y seguridad', 5, 'init'),
    ('PARAMETROS',    'Parámetros y configuración',  6, 'init');

-- Admin semilla: contraseña temporal 'admin123', hash generado con pgcrypto
-- (bcrypt, coste 12) — mismo algoritmo que usa PasswordHasher en Java, así
-- que el login funciona igual que si la app lo hubiera generado.
-- requiere_cambio_pwd=TRUE fuerza el cambio en el primer ingreso.
INSERT INTO sc_seguridad.usuario (persona_id, username, password_hash, algoritmo, requiere_cambio_pwd, estado, estacion)
VALUES (NULL, 'admin', crypt('admin123', gen_salt('bf', 12)), 'BCRYPT', TRUE, 'V', 'init');

INSERT INTO sc_seguridad.usuario_rol (usuario_id, rol_id, estacion)
SELECT u.usuario_id, r.rol_id, 'init'
FROM sc_seguridad.usuario u, sc_seguridad.rol r
WHERE u.username = 'admin' AND r.codigo = 'ADMIN';
