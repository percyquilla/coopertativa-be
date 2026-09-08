-- sc_parametros (CONTEXTO-BD.md §5.4 / §7 Paso 4). No hay archivo de diseño
-- base para este módulo: las tablas se construyen desde la descripción del
-- documento, siguiendo las convenciones del §3 (PK <entidad>_id IDENTITY,
-- columnas de auditoría usuario/fecha_hora/estacion, vigencias con
-- fecha_inicio/fecha_fin, índice explícito por cada FK).
-- Sin contraparte Java todavía (no existe paquete `parametros` en com.bien).

CREATE SCHEMA IF NOT EXISTS sc_parametros AUTHORIZATION bienbcb;

-- ============================================================
-- parametro_general — configuración clave/valor del sistema
-- ============================================================

CREATE TABLE sc_parametros.parametro_general (
    parametro_general_id BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo               VARCHAR(50)  NOT NULL,
    nombre               VARCHAR(150) NOT NULL,
    descripcion          VARCHAR(250),
    valor                VARCHAR(500) NOT NULL,
    tipo_dato            VARCHAR(20)  NOT NULL, -- usa los valores de dom_tipo_dato (TXT/NUM/DEC/DAT/DOM)
    editable             BOOLEAN      NOT NULL DEFAULT TRUE,
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_parametro_general PRIMARY KEY (parametro_general_id),
    CONSTRAINT uk_parametro_general_codigo UNIQUE (codigo)
);

-- ============================================================
-- institucion — datos de la cooperativa
-- ============================================================

CREATE TABLE sc_parametros.institucion (
    institucion_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo               VARCHAR(20)  NOT NULL,
    razon_social         VARCHAR(200) NOT NULL,
    nombre_comercial     VARCHAR(200),
    nit                  VARCHAR(20),
    matricula            VARCHAR(50),
    codigo_regulador     VARCHAR(20),
    direccion            VARCHAR(250),
    telefono             VARCHAR(30),
    correo               VARCHAR(120),
    moneda_base_id       BIGINT,
    representante_legal   VARCHAR(150),
    fecha_fundacion       DATE,
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_institucion PRIMARY KEY (institucion_id),
    CONSTRAINT uk_institucion_codigo UNIQUE (codigo),
    CONSTRAINT fk_institucion_moneda
        FOREIGN KEY (moneda_base_id) REFERENCES sc_administracion.moneda (moneda_id)
);

CREATE INDEX ix_institucion_moneda ON sc_parametros.institucion (moneda_base_id);

-- ============================================================
-- feriado — calendario de días no hábiles (RF-PAR-005)
-- Lo consume el proceso de fin de día / día contable.
-- ============================================================

CREATE TABLE sc_parametros.feriado (
    feriado_id           BIGINT GENERATED ALWAYS AS IDENTITY,
    fecha                DATE         NOT NULL,
    descripcion          VARCHAR(150) NOT NULL,
    ambito               VARCHAR(20)  NOT NULL DEFAULT 'NAC', -- NAC / DEP / LOC
    sucursal_id          BIGINT, -- solo cuando el feriado aplica a una sucursal puntual
    recurrente           BOOLEAN      NOT NULL DEFAULT FALSE, -- se repite cada año en la misma fecha
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_feriado PRIMARY KEY (feriado_id),
    CONSTRAINT fk_feriado_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sc_administracion.sucursal (sucursal_id)
);

CREATE INDEX ix_feriado_sucursal ON sc_parametros.feriado (sucursal_id);
CREATE UNIQUE INDEX uk_feriado_nacional
    ON sc_parametros.feriado (fecha) WHERE sucursal_id IS NULL;
CREATE UNIQUE INDEX uk_feriado_sucursal_fecha
    ON sc_parametros.feriado (fecha, sucursal_id) WHERE sucursal_id IS NOT NULL;

-- ============================================================
-- tipo_cambio — cotizaciones diarias (prioridad §5.4)
-- comprobante_reng.tipo_cambio ya se usa pero no había dónde guardarlas.
-- Una fila por par de monedas y fecha, con compra/venta/oficial.
-- ============================================================

CREATE TABLE sc_parametros.tipo_cambio (
    tipo_cambio_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    moneda_id            BIGINT       NOT NULL, -- moneda cotizada
    moneda_destino_id    BIGINT       NOT NULL, -- moneda en la que se expresa (normalmente la base)
    fecha                DATE         NOT NULL,
    compra               NUMERIC(20,10) NOT NULL,
    venta                NUMERIC(20,10) NOT NULL,
    oficial              NUMERIC(20,10),
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_tipo_cambio PRIMARY KEY (tipo_cambio_id),
    CONSTRAINT uk_tipo_cambio UNIQUE (moneda_id, moneda_destino_id, fecha),
    CONSTRAINT fk_tipo_cambio_moneda
        FOREIGN KEY (moneda_id) REFERENCES sc_administracion.moneda (moneda_id),
    CONSTRAINT fk_tipo_cambio_moneda_destino
        FOREIGN KEY (moneda_destino_id) REFERENCES sc_administracion.moneda (moneda_id),
    CONSTRAINT ck_tipo_cambio_monedas
        CHECK (moneda_id <> moneda_destino_id),
    CONSTRAINT ck_tipo_cambio_valores
        CHECK (compra > 0 AND venta > 0 AND (oficial IS NULL OR oficial > 0))
);

CREATE INDEX ix_tipo_cambio_moneda ON sc_parametros.tipo_cambio (moneda_id);
CREATE INDEX ix_tipo_cambio_moneda_destino ON sc_parametros.tipo_cambio (moneda_destino_id);
CREATE INDEX ix_tipo_cambio_fecha ON sc_parametros.tipo_cambio (fecha);

-- ============================================================
-- Datos semilla
-- ============================================================

INSERT INTO sc_parametros.parametro_general (codigo, nombre, descripcion, valor, tipo_dato, editable, estacion) VALUES
    ('GESTION_ACTUAL',       'Gestión contable actual',                'Año de la gestión contable en curso.',                 EXTRACT(YEAR FROM CURRENT_DATE)::TEXT, 'NUM', TRUE,  'init'),
    ('MAX_INTENTOS_LOGIN',   'Máximo de intentos de login',            'Intentos fallidos antes de bloquear la cuenta.',       '3',   'NUM', TRUE,  'init'),
    ('DIAS_HISTORICO_PWD',   'Contraseñas en el histórico',            'Cuántas contraseñas previas no se pueden reutilizar.', '5',   'NUM', TRUE,  'init'),
    ('DIAS_VIGENCIA_PWD',    'Vigencia de la contraseña (días)',       'Días antes de exigir cambio de contraseña.',           '90',  'NUM', TRUE,  'init'),
    ('PERMITE_LOGIN_MULTIPLE','Permitir sesiones simultáneas',         'Si un usuario puede tener varias sesiones activas.',    'true','TXT', TRUE,  'init');

-- Fila única de institución, con texto claramente provisional para completar.
INSERT INTO sc_parametros.institucion (codigo, razon_social, nombre_comercial, estacion) VALUES
    ('PRINCIPAL', 'Cooperativa (definir razón social)', 'BienBCB', 'init');

-- Feriados recurrentes de alcance universal; el calendario completo del país
-- debe cargarse por despliegue.
INSERT INTO sc_parametros.feriado (fecha, descripcion, ambito, recurrente, estacion) VALUES
    (DATE '2000-01-01', 'Año Nuevo',        'NAC', TRUE, 'init'),
    (DATE '2000-05-01', 'Día del Trabajo',  'NAC', TRUE, 'init'),
    (DATE '2000-12-25', 'Navidad',          'NAC', TRUE, 'init');

-- tipo_cambio no se siembra: las cotizaciones se cargan operativamente cada día.
