-- Extensión de sc_socios (CONTEXTO-BD.md §5.1 / §7 Paso 5). Sin archivo de
-- diseño base: se construye desde la descripción del documento, con las
-- convenciones del §3. `persona` sigue siendo el registro genérico de
-- personas (empleados, cotitulares, beneficiarios); `socio` es aparte.
--
-- Java en este paso: solo el agregado `socio` (entity/repo/dto/rest/service).
-- Las tablas satélite (persona_direccion/contacto/documento, beneficiario,
-- solicitud_retiro) quedan creadas pero sin CRUD Java todavía.

-- ============================================================
-- socio — separado de persona (RF-SOC)
-- ============================================================

CREATE TABLE sc_socios.socio (
    socio_id        BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id      BIGINT       NOT NULL,
    numero_socio    VARCHAR(20)  NOT NULL,
    sucursal_id     BIGINT       NOT NULL,
    fecha_ingreso   DATE         NOT NULL DEFAULT CURRENT_DATE,
    fecha_retiro    DATE,
    estado          VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario         VARCHAR(50),
    fecha_hora      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion        VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_socio PRIMARY KEY (socio_id),
    CONSTRAINT uk_socio_persona UNIQUE (persona_id),
    CONSTRAINT uk_socio_numero UNIQUE (numero_socio),
    CONSTRAINT fk_socio_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id),
    CONSTRAINT fk_socio_sucursal
        FOREIGN KEY (sucursal_id) REFERENCES sc_administracion.sucursal (sucursal_id),
    CONSTRAINT ck_socio_fechas
        CHECK (fecha_retiro IS NULL OR fecha_retiro >= fecha_ingreso)
);

CREATE INDEX ix_socio_sucursal ON sc_socios.socio (sucursal_id);

-- ============================================================
-- Contactos, direcciones y documentos de la persona (RF-SOC-001)
-- ============================================================

CREATE TABLE sc_socios.persona_direccion (
    persona_direccion_id BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id           BIGINT       NOT NULL,
    tipo                 VARCHAR(20)  NOT NULL, -- DOMICILIO / TRABAJO / CORRESPONDENCIA
    departamento         VARCHAR(60),
    ciudad               VARCHAR(60),
    zona                 VARCHAR(80),
    direccion            VARCHAR(250) NOT NULL,
    referencia           VARCHAR(250),
    es_principal         BOOLEAN      NOT NULL DEFAULT FALSE,
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_persona_direccion PRIMARY KEY (persona_direccion_id),
    CONSTRAINT fk_pdireccion_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id)
);

CREATE INDEX ix_pdireccion_persona ON sc_socios.persona_direccion (persona_id);

CREATE TABLE sc_socios.persona_contacto (
    persona_contacto_id BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id          BIGINT       NOT NULL,
    tipo                VARCHAR(20)  NOT NULL, -- TELEFONO / CELULAR / EMAIL / OTRO
    valor               VARCHAR(120) NOT NULL,
    es_principal        BOOLEAN      NOT NULL DEFAULT FALSE,
    estado              VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario             VARCHAR(50),
    fecha_hora          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion            VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_persona_contacto PRIMARY KEY (persona_contacto_id),
    CONSTRAINT fk_pcontacto_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id)
);

CREATE INDEX ix_pcontacto_persona ON sc_socios.persona_contacto (persona_id);

CREATE TABLE sc_socios.persona_documento (
    persona_documento_id BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id           BIGINT       NOT NULL,
    tipo                 VARCHAR(20)  NOT NULL, -- CI / PASAPORTE / CROQUIS / RESPALDO / OTRO
    numero               VARCHAR(50),
    descripcion          VARCHAR(200),
    ruta_archivo         VARCHAR(300), -- ubicación del escaneo; el blob no vive en la BD
    fecha_emision        DATE,
    fecha_vencimiento    DATE,
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_persona_documento PRIMARY KEY (persona_documento_id),
    CONSTRAINT fk_pdocumento_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id),
    CONSTRAINT ck_pdocumento_fechas
        CHECK (fecha_vencimiento IS NULL OR fecha_emision IS NULL
               OR fecha_vencimiento >= fecha_emision)
);

CREATE INDEX ix_pdocumento_persona ON sc_socios.persona_documento (persona_id);

-- ============================================================
-- beneficiario (indispensable en cooperativa)
-- ============================================================

CREATE TABLE sc_socios.beneficiario (
    beneficiario_id      BIGINT GENERATED ALWAYS AS IDENTITY,
    socio_id             BIGINT       NOT NULL,
    persona_id           BIGINT, -- si el beneficiario está registrado como persona
    nombre_completo      VARCHAR(200) NOT NULL, -- denormalizado por si persona_id es NULL
    parentesco           VARCHAR(30)  NOT NULL,
    porcentaje           NUMERIC(5,2) NOT NULL,
    documento_identidad  VARCHAR(20),
    fecha_nacimiento     DATE,
    estado               VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_beneficiario PRIMARY KEY (beneficiario_id),
    CONSTRAINT fk_beneficiario_socio
        FOREIGN KEY (socio_id) REFERENCES sc_socios.socio (socio_id),
    CONSTRAINT fk_beneficiario_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id),
    CONSTRAINT ck_beneficiario_porcentaje
        CHECK (porcentaje > 0 AND porcentaje <= 100)
);

CREATE INDEX ix_beneficiario_socio ON sc_socios.beneficiario (socio_id);
CREATE INDEX ix_beneficiario_persona ON sc_socios.beneficiario (persona_id);

-- ============================================================
-- solicitud_retiro con flujo de aprobación (RF-SOC-007)
-- ============================================================

CREATE TABLE sc_socios.solicitud_retiro (
    solicitud_retiro_id     BIGINT GENERATED ALWAYS AS IDENTITY,
    socio_id                BIGINT       NOT NULL,
    fecha_solicitud         DATE         NOT NULL DEFAULT CURRENT_DATE,
    motivo                  VARCHAR(300),
    estado                  VARCHAR(15)  NOT NULL DEFAULT 'PND', -- PND / APR / RCH / PGD / ANL
    monto_aportes           NUMERIC(18,2), -- calculado al aprobar
    fecha_resolucion        DATE,
    usuario_resolucion      VARCHAR(50),
    observacion_resolucion  VARCHAR(300),
    fecha_devolucion        DATE,
    usuario                 VARCHAR(50),
    fecha_hora              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion                VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_solicitud_retiro PRIMARY KEY (solicitud_retiro_id),
    CONSTRAINT fk_solretiro_socio
        FOREIGN KEY (socio_id) REFERENCES sc_socios.socio (socio_id),
    CONSTRAINT ck_solretiro_fechas
        CHECK (fecha_resolucion IS NULL OR fecha_resolucion >= fecha_solicitud)
);

CREATE INDEX ix_solretiro_socio ON sc_socios.solicitud_retiro (socio_id);
