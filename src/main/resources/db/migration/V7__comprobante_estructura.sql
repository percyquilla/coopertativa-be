-- tipo_comprobante, numeración correlativa y estructura_reporte
-- (CONTEXTO-BD.md §5.3 / §7 Paso 8). Sin archivo de diseño base.
-- Sin Java: la generación de reportes (RF-CON-014/015) y la asignación de
-- número al autorizar el comprobante son lógica de aplicación aparte.

-- ============================================================
-- tipo_comprobante como maestro (RF-CON-003)
-- Reemplaza el VARCHAR libre `comprobante.tipo_comprobante`.
-- ============================================================

CREATE TABLE sc_contabilidad.tipo_comprobante (
    tipo_comprobante_id BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo              VARCHAR(10)  NOT NULL,
    nombre              VARCHAR(100) NOT NULL,
    descripcion         VARCHAR(250),
    prefijo             VARCHAR(10),
    estado              VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario             VARCHAR(50),
    fecha_hora          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion            VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_tipo_comprobante PRIMARY KEY (tipo_comprobante_id),
    CONSTRAINT uk_tipo_comprobante_codigo UNIQUE (codigo)
);

INSERT INTO sc_contabilidad.tipo_comprobante (codigo, nombre, descripcion, prefijo, estacion) VALUES
    ('ING', 'Comprobante de ingreso',  'Movimientos que ingresan efectivo o equivalentes.', 'ING', 'init'),
    ('EGR', 'Comprobante de egreso',   'Movimientos que erogan efectivo o equivalentes.',   'EGR', 'init'),
    ('DIA', 'Comprobante de diario',   'Ajustes y asientos que no mueven caja.',            'DIA', 'init'),
    ('TRA', 'Comprobante de traspaso', 'Traspasos entre cuentas internas.',                 'TRA', 'init');

ALTER TABLE sc_contabilidad.comprobante
    DROP COLUMN tipo_comprobante,
    ADD COLUMN tipo_comprobante_id BIGINT NOT NULL,
    ADD COLUMN numero BIGINT;

ALTER TABLE sc_contabilidad.comprobante
    ADD CONSTRAINT fk_comprobante_tipo
        FOREIGN KEY (tipo_comprobante_id)
        REFERENCES sc_contabilidad.tipo_comprobante (tipo_comprobante_id),
    ADD CONSTRAINT uk_comprobante_numero
        UNIQUE (tipo_comprobante_id, periodo_id, numero);

CREATE INDEX ix_comprobante_tipo ON sc_contabilidad.comprobante (tipo_comprobante_id);

-- ============================================================
-- Numeración correlativa por tipo de comprobante y período
-- ============================================================

CREATE TABLE sc_contabilidad.comprobante_secuencia (
    comprobante_secuencia_id BIGINT GENERATED ALWAYS AS IDENTITY,
    tipo_comprobante_id      BIGINT       NOT NULL,
    periodo_id               BIGINT       NOT NULL,
    ultimo_numero            BIGINT       NOT NULL DEFAULT 0,
    usuario                  VARCHAR(50),
    fecha_hora               TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion                 VARCHAR(30)  NOT NULL DEFAULT 'init',

    CONSTRAINT pk_comprobante_secuencia PRIMARY KEY (comprobante_secuencia_id),
    CONSTRAINT uk_comprobante_secuencia UNIQUE (tipo_comprobante_id, periodo_id),
    CONSTRAINT fk_cseq_tipo
        FOREIGN KEY (tipo_comprobante_id)
        REFERENCES sc_contabilidad.tipo_comprobante (tipo_comprobante_id),
    CONSTRAINT fk_cseq_periodo
        FOREIGN KEY (periodo_id)
        REFERENCES sc_contabilidad.periodo_contable (periodo_id),
    CONSTRAINT ck_cseq_numero CHECK (ultimo_numero >= 0)
);

CREATE INDEX ix_cseq_periodo ON sc_contabilidad.comprobante_secuencia (periodo_id);

-- Correlativo siguiente, seguro ante concurrencia.
CREATE OR REPLACE FUNCTION sc_contabilidad.fn_siguiente_numero_comprobante(
    p_tipo_comprobante_id BIGINT,
    p_periodo_id          BIGINT
) RETURNS BIGINT AS $$
DECLARE
    v_numero BIGINT;
BEGIN
    INSERT INTO sc_contabilidad.comprobante_secuencia (tipo_comprobante_id, periodo_id, ultimo_numero)
    VALUES (p_tipo_comprobante_id, p_periodo_id, 1)
    ON CONFLICT (tipo_comprobante_id, periodo_id)
        DO UPDATE SET ultimo_numero = sc_contabilidad.comprobante_secuencia.ultimo_numero + 1
    RETURNING ultimo_numero INTO v_numero;

    RETURN v_numero;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- estructura_reporte — mapeo de cuentas a líneas de reporte
-- (RF-CON-014 estado de situación financiera / RF-CON-015 estado de resultados)
-- ============================================================

CREATE TABLE sc_contabilidad.estructura_reporte (
    estructura_reporte_id BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo                VARCHAR(30)  NOT NULL,
    nombre                VARCHAR(150) NOT NULL,
    tipo                  VARCHAR(20)  NOT NULL, -- ESF / ER
    estado                VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario               VARCHAR(50),
    fecha_hora            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion              VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_estructura_reporte PRIMARY KEY (estructura_reporte_id),
    CONSTRAINT uk_estructura_reporte_codigo UNIQUE (codigo)
);

CREATE TABLE sc_contabilidad.estructura_reporte_linea (
    estructura_reporte_linea_id BIGINT GENERATED ALWAYS AS IDENTITY,
    estructura_reporte_id       BIGINT       NOT NULL,
    linea_padre_id              BIGINT,
    codigo                      VARCHAR(30)  NOT NULL,
    titulo                      VARCHAR(200) NOT NULL,
    orden                       INTEGER      NOT NULL,
    tipo_linea                  VARCHAR(20)  NOT NULL, -- TITULO / DETALLE / SUBTOTAL / TOTAL
    signo                       SMALLINT     NOT NULL DEFAULT 1,
    formula                     VARCHAR(500),
    estado                      VARCHAR(3)   NOT NULL DEFAULT 'V',
    usuario                     VARCHAR(50),
    fecha_hora                  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion                    VARCHAR(30)  NOT NULL,

    CONSTRAINT pk_estructura_reporte_linea PRIMARY KEY (estructura_reporte_linea_id),
    CONSTRAINT uk_estructura_reporte_linea UNIQUE (estructura_reporte_id, codigo),
    CONSTRAINT fk_erl_reporte
        FOREIGN KEY (estructura_reporte_id)
        REFERENCES sc_contabilidad.estructura_reporte (estructura_reporte_id),
    CONSTRAINT fk_erl_padre
        FOREIGN KEY (linea_padre_id)
        REFERENCES sc_contabilidad.estructura_reporte_linea (estructura_reporte_linea_id),
    CONSTRAINT ck_erl_signo CHECK (signo IN (-1, 1))
);

CREATE INDEX ix_erl_reporte ON sc_contabilidad.estructura_reporte_linea (estructura_reporte_id);
CREATE INDEX ix_erl_padre ON sc_contabilidad.estructura_reporte_linea (linea_padre_id);

CREATE TABLE sc_contabilidad.estructura_reporte_cuenta (
    estructura_reporte_cuenta_id BIGINT GENERATED ALWAYS AS IDENTITY,
    estructura_reporte_linea_id  BIGINT       NOT NULL,
    cuenta_id                    BIGINT       NOT NULL,
    signo                        SMALLINT     NOT NULL DEFAULT 1,
    usuario                      VARCHAR(50),
    fecha_hora                   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion                     VARCHAR(30)  NOT NULL DEFAULT 'init',

    CONSTRAINT pk_estructura_reporte_cuenta PRIMARY KEY (estructura_reporte_cuenta_id),
    CONSTRAINT uk_estructura_reporte_cuenta UNIQUE (estructura_reporte_linea_id, cuenta_id),
    CONSTRAINT fk_erc_linea
        FOREIGN KEY (estructura_reporte_linea_id)
        REFERENCES sc_contabilidad.estructura_reporte_linea (estructura_reporte_linea_id),
    CONSTRAINT fk_erc_cuenta
        FOREIGN KEY (cuenta_id)
        REFERENCES sc_contabilidad.cuenta (cuenta_id),
    CONSTRAINT ck_erc_signo CHECK (signo IN (-1, 1))
);

CREATE INDEX ix_erc_linea ON sc_contabilidad.estructura_reporte_cuenta (estructura_reporte_linea_id);
CREATE INDEX ix_erc_cuenta ON sc_contabilidad.estructura_reporte_cuenta (cuenta_id);

INSERT INTO sc_contabilidad.estructura_reporte (codigo, nombre, tipo, estacion) VALUES
    ('ESF', 'Estado de Situación Financiera', 'ESF', 'init'),
    ('ER',  'Estado de Resultados',           'ER',  'init');
