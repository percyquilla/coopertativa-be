-- Diseño base (CONTEXTO-BD.md §2/§3) con los parches de §4 ya aplicados.
-- Los esquemas sc_socios/sc_administracion/sc_captaciones/sc_contabilidad
-- reemplazan a bd/ddl.sql (que usaba id_persona/id_usuario en vez de
-- persona_id/usuario_id). sc_seguridad se carga aparte en V2 (Paso 3).

CREATE SCHEMA IF NOT EXISTS sc_socios AUTHORIZATION bienbcb;
CREATE SCHEMA IF NOT EXISTS sc_administracion AUTHORIZATION bienbcb;
CREATE SCHEMA IF NOT EXISTS sc_captaciones AUTHORIZATION bienbcb;
CREATE SCHEMA IF NOT EXISTS sc_contabilidad AUTHORIZATION bienbcb;
CREATE SCHEMA IF NOT EXISTS sc_seguridad AUTHORIZATION bienbcb;

-- ============================================================
-- sc_socios
-- ============================================================

CREATE TABLE sc_socios.persona
    (
        persona_id           BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
        apellido_paterno     VARCHAR(50) NOT NULL,
        apellido_materno     VARCHAR(50) NOT NULL,
        apellido_casada      VARCHAR(50),
        nombres              VARCHAR(60) NOT NULL,
        cedula_identidad     VARCHAR(20) NOT NULL,
        expedicion           VARCHAR(5),
        genero               VARCHAR(2) NOT NULL,
        lugar_trabajo        VARCHAR(80),
        cargo_actual         VARCHAR(150),
        correo_institucional VARCHAR(80),
        correo_personal      VARCHAR(80),
        numero_telefono      VARCHAR(10), -- parche §4.7: ya no NOT NULL (va a tabla de contacto a futuro)
        fecha_nacimiento     DATE        NOT NULL,
        estado_civil         VARCHAR(20) NOT NULL,
        estado               VARCHAR(3) NOT NULL,
        usuario              VARCHAR(50),
        fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        estacion             VARCHAR(30) NOT NULL,
        CONSTRAINT pk_persona PRIMARY KEY (persona_id), -- parche: nombre corregido (era pk_socio)
        CONSTRAINT uk_persona_cedula UNIQUE (cedula_identidad, expedicion) -- parche §4.6
    );

CREATE TABLE sc_socios.certificado_aportacion (
    certificado_id         BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id             BIGINT NOT NULL,
    monto_estado           NUMERIC(18,2) NOT NULL DEFAULT 0,
    estado_verif           VARCHAR(15) NOT NULL DEFAULT 'PND',
    estado                 VARCHAR(15) NOT NULL DEFAULT 'REG',
    fecha                  DATE NOT NULL,
    usuario                VARCHAR(50),
    fecha_hora             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion               VARCHAR(30) NOT NULL,

    CONSTRAINT pk_certificado PRIMARY KEY (certificado_id),

    CONSTRAINT fk_certif_persona FOREIGN KEY (persona_id)
        REFERENCES sc_socios.persona (persona_id)
);

-- ============================================================
-- sc_seguridad — puente temporal
-- Misma forma que bd/ddl.sql (username/password/salt/rol de texto libre,
-- SHA-256), solo con la convención de PK unificada (usuario_id/persona_id)
-- para que Usuario.java siga teniendo tabla contra la que mapear. El
-- rehecho completo (BCrypt, tablas rol/permiso/modulo/sesion) es el Paso 3
-- de CONTEXTO-BD.md §7 — aparte, por su mayor riesgo.
-- ============================================================

CREATE TABLE sc_seguridad.usuario (
    usuario_id  BIGINT GENERATED ALWAYS AS IDENTITY,
    persona_id  BIGINT,
    username    VARCHAR(50) NOT NULL,
    password    VARCHAR(100) NOT NULL,
    salt        VARCHAR(64) NOT NULL,
    rol         VARCHAR(20) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_hora  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_usuario
        PRIMARY KEY (usuario_id),
    CONSTRAINT uq_usuario_username
        UNIQUE (username),
    CONSTRAINT uq_usuario_persona
        UNIQUE (persona_id),
    CONSTRAINT fk_usuario_persona
        FOREIGN KEY (persona_id) REFERENCES sc_socios.persona (persona_id),
    CONSTRAINT ck_usuario_rol
        CHECK (rol IN ('ADMIN', 'USER'))
);

CREATE INDEX ix_usuario_persona ON sc_seguridad.usuario (persona_id); -- parche §4.8

-- Usuario semilla (username: admin / password: admin123, hash SHA-256 con salt aleatorio)
-- Sigue expuesto en texto plano aquí a propósito de continuidad con bd/ddl.sql;
-- CONTEXTO-BD.md §5.2 exige rotarlo y dejar de versionarlo — se resuelve en el Paso 3.
INSERT INTO sc_seguridad.usuario (username, password, salt, rol, activo)
VALUES ('admin', 'f97ba39c47aa876240161aedb9d3c9d44935e148769fc7472d8a06b21a904b0c', '4f06293f8315ffe881ab4447de6b4862', 'ADMIN', TRUE);

-- ============================================================
-- sc_administracion
-- ============================================================

CREATE TABLE sc_administracion.dominio
(
    dominio_id  VARCHAR(30) NOT NULL,
    descripcion VARCHAR(128) NOT NULL,
    estado      VARCHAR(3) NOT NULL,
    usuario     VARCHAR(50) NOT NULL,
    fecha_hora  TIMESTAMP(6) NOT NULL,
    estacion    VARCHAR(30) NOT NULL,

    CONSTRAINT pk_dominio
        PRIMARY KEY (dominio_id)
);

CREATE TABLE sc_administracion.dominio_valor
    (   dominio_valor_id  BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
        dominio_id        VARCHAR(30) NOT NULL, -- parche §4.2: antes VARCHAR(20), no coincidía con dominio.dominio_id
        valor             VARCHAR(20) NOT NULL,
        descripcion_valor VARCHAR(100) NOT NULL,
        estado            VARCHAR(3) NOT NULL,
        cod_usuario       VARCHAR(50) NOT NULL,
        fecha_hora        TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
        estacion          VARCHAR(30) NOT NULL,
        CONSTRAINT pk_dominio_valor PRIMARY KEY (dominio_valor_id),
        CONSTRAINT uk_dominio_valor UNIQUE (dominio_id, valor),

        CONSTRAINT fk_dominio_valor FOREIGN KEY (dominio_id)
        REFERENCES sc_administracion.dominio (dominio_id)
    );

CREATE TABLE sc_administracion.producto (
    producto_id            BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo                 VARCHAR(20)  NOT NULL,
    nombre                 VARCHAR(100) NOT NULL,
    descripcion            VARCHAR(250),
    tipo_producto          VARCHAR(20)  NOT NULL,
    moneda                 VARCHAR(10)  NOT NULL,
    saldo_minimo           NUMERIC(18,2) NOT NULL DEFAULT 0,
    permite_deposito       BOOLEAN NOT NULL DEFAULT TRUE,
    permite_retiro         BOOLEAN NOT NULL DEFAULT TRUE,
    permite_interes        BOOLEAN NOT NULL DEFAULT TRUE,
    estado                 VARCHAR(15) NOT NULL DEFAULT 'V',
    fecha_inicio           DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin              DATE,
    usuario                VARCHAR(50),
    fecha_hora             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_producto PRIMARY KEY (producto_id),
    CONSTRAINT uk_producto_codigo UNIQUE (codigo),

    CONSTRAINT ck_producto_captacion_saldo
        CHECK (saldo_minimo >= 0),

    CONSTRAINT ck_producto_fechas
        CHECK (
            fecha_fin IS NULL
            OR fecha_fin >= fecha_inicio
        )
);

CREATE TABLE sc_administracion.producto_tasa (
    producto_tasa_id           BIGINT GENERATED ALWAYS AS IDENTITY,
    producto_id                BIGINT NOT NULL,
    tasa                       NUMERIC(10,6) NOT NULL,
    tipo_tasa                  VARCHAR(20) NOT NULL,
    base_dias                  INTEGER NOT NULL DEFAULT 360,
    fecha_inicio               DATE NOT NULL,
    fecha_fin                  DATE,
    estado                     VARCHAR(15) NOT NULL DEFAULT 'V',
    usuario                    VARCHAR(50),
    fecha_hora                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_producto_tasa PRIMARY KEY (producto_tasa_id),

    CONSTRAINT fk_producto_tasa FOREIGN KEY (producto_id)
    REFERENCES sc_administracion.producto(producto_id),

    CONSTRAINT ck_producto_tasa_valor
        CHECK (tasa >= 0),

    CONSTRAINT ck_producto_tasa_fechas
        CHECK (
            fecha_fin IS NULL
            OR fecha_fin >= fecha_inicio
        )
);

CREATE TABLE sc_administracion.producto_parametro (
    producto_parametro_id BIGINT GENERATED ALWAYS AS IDENTITY,
    producto_id           BIGINT NOT NULL,
    codigo                VARCHAR(30) NOT NULL,
    nombre                VARCHAR(100) NOT NULL,
    valor                 VARCHAR(250) NOT NULL,
    tipo_dato             VARCHAR(20) NOT NULL,
    id_dominio            VARCHAR(30),
    fecha_inicio          DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin             DATE,
    estado                VARCHAR(15) NOT NULL DEFAULT 'V',
    usuario               VARCHAR(50),
    fecha_hora            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_producto_parametro
        PRIMARY KEY (producto_parametro_id),

    CONSTRAINT fk_producto_param
        FOREIGN KEY (producto_id)
        REFERENCES sc_administracion.producto(producto_id),

    CONSTRAINT uk_producto_parametro
        UNIQUE ( producto_id, codigo, fecha_inicio)
);

CREATE TABLE sc_administracion.tipo_operacion (
    tipo_operacion_id      BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo                 VARCHAR(20)  NOT NULL,
    descripcion            VARCHAR(250),
    estado                 VARCHAR(15) NOT NULL DEFAULT 'V',
    fecha_inicio           DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin              DATE,
    usuario                VARCHAR(50),
    fecha_hora             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_tipo_operacion PRIMARY KEY (tipo_operacion_id),
    CONSTRAINT uk_tipo_oper_codigo UNIQUE (codigo),

    CONSTRAINT ck_tipo_oper_fechas
        CHECK (
            fecha_fin IS NULL
            OR fecha_fin >= fecha_inicio
        )
);

CREATE TABLE sc_administracion.sucursal (
    sucursal_id         BIGINT GENERATED BY DEFAULT AS IDENTITY,
    codigo              VARCHAR(20) NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    estado              CHAR(1) NOT NULL DEFAULT 'V',

    CONSTRAINT pk_sucursal
        PRIMARY KEY (sucursal_id),

    CONSTRAINT uk_sucursal_codigo UNIQUE (codigo)
);

CREATE TABLE sc_administracion.moneda (
    moneda_id           BIGINT GENERATED BY DEFAULT AS IDENTITY,
    codigo              VARCHAR(10) NOT NULL,
    desripcion          VARCHAR(100) NOT NULL,
    simbolo             VARCHAR(10),
    estado              VARCHAR(2) NOT NULL DEFAULT 'V',

    CONSTRAINT pk_moneda
        PRIMARY KEY (moneda_id),

    CONSTRAINT uk_moneda_codigo
        UNIQUE (codigo)
);

-- ============================================================
-- sc_captaciones
-- (los DROP TABLE especulativos de sc_captaciones.sql, con nombres que ya no
-- existen (§4.5), se omiten: no aplican dentro de una migración versionada)
-- ============================================================

CREATE TABLE sc_captaciones.cuenta_captacion (
    cuenta_captacion_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    numero_cuenta         VARCHAR(30) NOT NULL,
    producto_id           BIGINT NOT NULL,
    tipo_cuenta           VARCHAR(5) NOT NULL,
    moneda                VARCHAR(10) NOT NULL,
    fecha_apertura        DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_cierre          DATE,
    saldo_actual          NUMERIC(18,2) NOT NULL DEFAULT 0,
    saldo_bloqueado       NUMERIC(18,2) NOT NULL DEFAULT 0,
    estado_cuenta         VARCHAR(20) NOT NULL DEFAULT 'A',
    usuario               VARCHAR(50),
    fecha_hora            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion              VARCHAR(30) NOT NULL,

    CONSTRAINT pk_cuenta_captacion
        PRIMARY KEY (cuenta_captacion_id),

    CONSTRAINT uk_cuenta_captacion_numero
        UNIQUE (numero_cuenta),

    CONSTRAINT fk_cuenta_captacion_producto
        FOREIGN KEY (producto_id)
        REFERENCES sc_administracion.producto
        (producto_id),

    CONSTRAINT ck_cuenta_captacion_saldo
        CHECK ( saldo_actual >= 0
            AND saldo_bloqueado >= 0
            AND saldo_bloqueado <= saldo_actual),

    CONSTRAINT ck_cuenta_captacion_fechas
        CHECK ( fecha_cierre IS NULL OR fecha_cierre >= fecha_apertura)
);

CREATE TABLE sc_captaciones.cuenta_titular (
    cuenta_titular_id   BIGINT GENERATED ALWAYS AS IDENTITY,
    cuenta_captacion_id BIGINT NOT NULL,
    persona_id          BIGINT NOT NULL,
    tipo_titular        VARCHAR(20) NOT NULL,
    fecha_inicio        DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin           DATE,
    estado              VARCHAR(15) NOT NULL DEFAULT 'V',
    usuario             VARCHAR(50),
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estacion            VARCHAR(30) NOT NULL,

    CONSTRAINT pk_cuenta_titular
        PRIMARY KEY (cuenta_titular_id),

    CONSTRAINT fk_cuenta_titular_cuenta
        FOREIGN KEY (cuenta_captacion_id)
        REFERENCES sc_captaciones.cuenta_captacion
        (cuenta_captacion_id),

    CONSTRAINT fk_cuenta_titular_persona
        FOREIGN KEY (persona_id)
        REFERENCES sc_socios.persona(persona_id),

    CONSTRAINT uk_cuenta_titular
        UNIQUE (
            cuenta_captacion_id,
            persona_id,
            fecha_inicio
        ),

    CONSTRAINT ck_cuenta_titular_fechas
        CHECK (
            fecha_fin IS NULL
            OR fecha_fin >= fecha_inicio
        )
);

CREATE TABLE sc_captaciones.movimiento (
    movimiento_id         BIGINT GENERATED ALWAYS AS IDENTITY,
    cuenta_captacion_id   BIGINT NOT NULL,
    tipo_movimiento       VARCHAR(30) NOT NULL,
    fecha_hora            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto                 NUMERIC(18,2) NOT NULL,
    saldo_anterior        NUMERIC(18,2) NOT NULL,
    saldo_posterior       NUMERIC(18,2) NOT NULL,
    referencia            VARCHAR(100),
    descripcion           VARCHAR(250),
    estado_mov            VARCHAR(20) NOT NULL DEFAULT 'REG',
    usuario               VARCHAR(50),

    CONSTRAINT pk_movimiento
        PRIMARY KEY (movimiento_id),

    CONSTRAINT fk_movimiento_cuenta
        FOREIGN KEY (cuenta_captacion_id)
        REFERENCES sc_captaciones.cuenta_captacion
        (cuenta_captacion_id),

    CONSTRAINT ck_movimiento_captacion_monto
        CHECK (monto > 0),

    CONSTRAINT ck_movimiento_captacion_saldos
        CHECK (
            saldo_anterior >= 0
            AND saldo_posterior >= 0
        )
);

CREATE TABLE sc_captaciones.interes_captacion (
    interes_captacion_id BIGINT GENERATED ALWAYS AS IDENTITY,
    cuenta_captacion_id  BIGINT NOT NULL,
    fecha_inicio_periodo DATE NOT NULL,
    fecha_fin_periodo    DATE NOT NULL,
    saldo_base           NUMERIC(18,2) NOT NULL,
    tasa                 NUMERIC(10,6) NOT NULL,
    dias                 INTEGER NOT NULL,
    interes_calculado    NUMERIC(18,2) NOT NULL,
    interes_devengado    NUMERIC(18,2) NOT NULL DEFAULT 0,
    estado               VARCHAR(20) NOT NULL DEFAULT 'REG',
    fecha_calculo        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario              VARCHAR(50),

    CONSTRAINT pk_interes_captacion
        PRIMARY KEY (interes_captacion_id),

    CONSTRAINT fk_interes_captacion_cuenta
        FOREIGN KEY (cuenta_captacion_id)
        REFERENCES sc_captaciones.cuenta_captacion
        (cuenta_captacion_id),

    CONSTRAINT ck_interes_captacion_fechas
        CHECK (
            fecha_fin_periodo >= fecha_inicio_periodo
        ),

    CONSTRAINT ck_interes_captacion_saldo
        CHECK (saldo_base >= 0),

    CONSTRAINT ck_interes_captacion_tasa
        CHECK (tasa >= 0),

    CONSTRAINT ck_interes_captacion_dias
        CHECK (dias > 0),

    CONSTRAINT ck_interes_captacion_interes
        CHECK (
            interes_calculado >= 0
            AND interes_devengado >= 0
        )

);

CREATE TABLE sc_captaciones.interes_capitalizacion (
    interes_capitalizacion_id BIGINT GENERATED ALWAYS AS IDENTITY,
    interes_captacion_id      BIGINT NOT NULL,
    cuenta_captacion_id       BIGINT NOT NULL,
    fecha_capitalizacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_interes             NUMERIC(18,2) NOT NULL,
    saldo_anterior            NUMERIC(18,2) NOT NULL,
    saldo_posterior           NUMERIC(18,2) NOT NULL,
    estado                    VARCHAR(20) NOT NULL DEFAULT 'REG',
    usuario                   VARCHAR(50),

    CONSTRAINT pk_interes_capitalizacion
        PRIMARY KEY (interes_capitalizacion_id),

    CONSTRAINT fk_capitalizacion_interes
        FOREIGN KEY (interes_captacion_id)
        REFERENCES sc_captaciones.interes_captacion
        (interes_captacion_id),

    CONSTRAINT fk_capitalizacion_cuenta
        FOREIGN KEY (cuenta_captacion_id)
        REFERENCES sc_captaciones.cuenta_captacion
        (cuenta_captacion_id),

    CONSTRAINT uk_capitalizacion_interes
        UNIQUE (interes_captacion_id),

    CONSTRAINT ck_capitalizacion_monto
        CHECK (monto_interes > 0),

    CONSTRAINT ck_capitalizacion_saldos
        CHECK (
            saldo_anterior >= 0
            AND saldo_posterior >= saldo_anterior
        )

);

-- ============================================================
-- sc_contabilidad
-- ============================================================

CREATE TABLE sc_contabilidad.periodo_contable (
    periodo_id          BIGINT GENERATED BY DEFAULT AS IDENTITY,
    gestion             INTEGER NOT NULL,
    mes                 SMALLINT NOT NULL,
    fecha_inicio        DATE NOT NULL,
    fecha_fin           DATE NOT NULL,
    estado              VARCHAR(2) NOT NULL DEFAULT 'A',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_periodo_contable
        PRIMARY KEY (periodo_id),

    CONSTRAINT uk_periodo_gestion_mes
        UNIQUE (gestion, mes),

    CONSTRAINT ck_periodo_mes
        CHECK (mes BETWEEN 1 AND 12),

    CONSTRAINT ck_periodo_fechas
        CHECK (fecha_fin >= fecha_inicio)

);

CREATE TABLE sc_contabilidad.dia_contable (
    dia_id              BIGINT GENERATED BY DEFAULT AS IDENTITY,
    fecha               DATE NOT NULL,
    periodo_id          BIGINT,
    estado              VARCHAR(20) NOT NULL DEFAULT 'A',
    CONSTRAINT pk_dia_contable PRIMARY KEY (dia_id),

    CONSTRAINT uk_dia_contable UNIQUE (fecha),

    CONSTRAINT fk_dia_periodo FOREIGN KEY (periodo_id) -- parche §4.4: FK que faltaba
        REFERENCES sc_contabilidad.periodo_contable (periodo_id)
);

CREATE TABLE sc_contabilidad.clasificacion_cuenta (
    clasificacion_id    BIGINT GENERATED BY DEFAULT AS IDENTITY,
    codigo              VARCHAR(20) NOT NULL,
    dom_debe_haber      VARCHAR(2) NOT NULL,
    descripcion         VARCHAR(100) NOT NULL,
    estado              VARCHAR(2)  NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_tipo_cuenta
        PRIMARY KEY (clasificacion_id),

    CONSTRAINT uk_tipo_cuenta_codigo
        UNIQUE (codigo)
);

CREATE TABLE sc_contabilidad.cuenta (
    cuenta_id           BIGINT GENERATED BY DEFAULT AS IDENTITY,
    cuenta_padre_id     BIGINT,
    clasificacion_id    BIGINT NOT NULL,
    dom_tipo_cuenta     VARCHAR(5) NOT NULL,
    codigo              VARCHAR(30) NOT NULL,
    nombre              VARCHAR(200) NOT NULL,
    descripcion         VARCHAR(500),
    nivel               SMALLINT NOT NULL,
    estado              VARCHAR(2)  NOT NULL DEFAULT 'V',
    fecha_inicio        DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin           DATE,
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_cuenta
        PRIMARY KEY (cuenta_id),

    CONSTRAINT uk_cuenta_codigo
        UNIQUE (codigo),

    CONSTRAINT fk_cuenta_padre
        FOREIGN KEY (cuenta_padre_id)
        REFERENCES sc_contabilidad.cuenta(cuenta_id),

    CONSTRAINT fk_clasificacion
        FOREIGN KEY (clasificacion_id)
        REFERENCES sc_contabilidad.clasificacion_cuenta(clasificacion_id),

    CONSTRAINT ck_cuenta_nivel
        CHECK (nivel > 0),

    CONSTRAINT ck_cuenta_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE TABLE sc_contabilidad.centro (
    centro_id     BIGINT GENERATED BY DEFAULT AS IDENTITY,
    codigo        VARCHAR(30) NOT NULL,
    nombre        VARCHAR(150) NOT NULL,
    estado        CHAR(1) NOT NULL DEFAULT 'V',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_centro_costo PRIMARY KEY (centro_id),

    CONSTRAINT uk_centro_costo_codigo UNIQUE (codigo)

);

CREATE TABLE sc_contabilidad.cuenta_afectable (
    afectable_id     BIGINT GENERATED BY DEFAULT AS IDENTITY,
    cuenta_id        BIGINT NOT NULL,
    codigo           VARCHAR(30),
    moneda_id        BIGINT,
    sucursal_id      BIGINT,
    centro_id        BIGINT,
    producto_id      BIGINT,
    descripcion      VARCHAR(250),
    estado           VARCHAR(2)  NOT NULL DEFAULT 'V',
    fecha_inicio     DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin        DATE,
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_cuenta_afectable
        PRIMARY KEY (afectable_id),

    CONSTRAINT uk_cuenta_afectable_codigo
        UNIQUE (codigo),

    CONSTRAINT fk_ca_cuenta
        FOREIGN KEY (cuenta_id)
        REFERENCES sc_contabilidad.cuenta(cuenta_id),

    CONSTRAINT fk_ca_moneda
        FOREIGN KEY (moneda_id)
        REFERENCES sc_administracion.moneda(moneda_id),

    CONSTRAINT fk_ca_sucursal
        FOREIGN KEY (sucursal_id)
        REFERENCES sc_administracion.sucursal(sucursal_id),

    CONSTRAINT fk_ca_centro_costo
        FOREIGN KEY (centro_id)
        REFERENCES sc_contabilidad.centro(centro_id),

    CONSTRAINT fk_ca_producto
        FOREIGN KEY (producto_id)
        REFERENCES sc_administracion.producto(producto_id),

    CONSTRAINT ck_ca_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE TABLE sc_contabilidad.esquema_contable (
    esquema_contable_id   BIGINT GENERATED BY DEFAULT AS IDENTITY,
    codigo                VARCHAR(50) NOT NULL,
    nombre                VARCHAR(200) NOT NULL,
    version               INTEGER NOT NULL,
    estado                VARCHAR(2)  NOT NULL DEFAULT 'V',
    fecha_inicio          DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin             DATE,
    descripcion           VARCHAR(500),
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_esquema_contable
        PRIMARY KEY (esquema_contable_id),

    CONSTRAINT uk_esquema_contable
        UNIQUE (codigo, version),

    CONSTRAINT ck_regla_version
        CHECK (version > 0),

    CONSTRAINT ck_regla_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE TABLE sc_contabilidad.esquema_contable_detalle (
    esquema_detalle_id       BIGINT GENERATED BY DEFAULT AS IDENTITY,
    esquema_contable_id      BIGINT NOT NULL,
    secuencia                INTEGER NOT NULL,
    dom_debe_haber           VARCHAR(2),
    afectable_id             BIGINT NOT NULL,
    formula                  VARCHAR(500),
    descripcion              VARCHAR(250),

    CONSTRAINT pk_regla_contable_detalle
        PRIMARY KEY (esquema_detalle_id),

    CONSTRAINT uk_regla_detalle_secuencia
        UNIQUE (esquema_contable_id, secuencia),

    CONSTRAINT fk_rcd_regla
        FOREIGN KEY (esquema_contable_id)
        REFERENCES sc_contabilidad.esquema_contable(esquema_contable_id),

    CONSTRAINT fk_rcd_cuenta
        FOREIGN KEY (afectable_id)
        REFERENCES sc_contabilidad.cuenta_afectable(afectable_id),

    CONSTRAINT ck_rcd_secuencia
        CHECK (secuencia > 0)
);

CREATE TABLE sc_contabilidad.operacion_contable (
    operacion_id           BIGINT GENERATED BY DEFAULT AS IDENTITY,
    tipo_operacion_id      BIGINT NOT NULL,
    fecha_operacion        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    glosa                  VARCHAR(200) not null,
    moneda_id              BIGINT not null,
    monto                  NUMERIC(20,6),
    estado                 VARCHAR(3)  NOT NULL DEFAULT 'REG',
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_operacion_contable
        PRIMARY KEY (operacion_id),

    CONSTRAINT fk_operacion_moneda
        FOREIGN KEY (moneda_id)
        REFERENCES sc_administracion.moneda(moneda_id),

    CONSTRAINT ck_operacion_monto
        CHECK (monto IS NULL OR monto >= 0)

);

CREATE TABLE sc_contabilidad.comprobante (
    comprobante_id         BIGINT GENERATED BY DEFAULT AS IDENTITY,
    periodo_id             BIGINT NOT NULL,
    operacion_id           BIGINT,
    tipo_comprobante       VARCHAR(10) NOT NULL,
    fecha                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    glosa                  VARCHAR(300),
    estado                 VARCHAR(3)  NOT NULL DEFAULT 'REG',
    usuario_creacion       VARCHAR(100),
    fecha_creacion         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_autoriz        VARCHAR(100),
    fecha_autoriz          TIMESTAMP,
    estacion               VARCHAR(30) NOT NULL,

    CONSTRAINT pk_comprobante
        PRIMARY KEY (comprobante_id),

    CONSTRAINT fk_comprobante_periodo
        FOREIGN KEY (periodo_id)
        REFERENCES sc_contabilidad.periodo_contable(periodo_id),

    CONSTRAINT fk_comprobante_operacion
        FOREIGN KEY (operacion_id)
        REFERENCES sc_contabilidad.operacion_contable(operacion_id)
);

CREATE TABLE sc_contabilidad.comprobante_reng (
    comprobante_reng_id       BIGINT GENERATED BY DEFAULT AS IDENTITY,
    comprobante_id             BIGINT NOT NULL,
    secuencia                 INTEGER NOT NULL,
    afectable_id              BIGINT NOT NULL,
    dom_debe_haber            VARCHAR(2) NOT NULL,
    monto                     NUMERIC(20,6) NOT NULL,
    moneda_id_mo              BIGINT NOT NULL, -- parche §4.3: antes INTEGER y sin FK
    monto_mo                  NUMERIC(20,6),
    tipo_cambio               NUMERIC(20,10),
    glosa                     VARCHAR(100),

    CONSTRAINT pk_asiento_linea
        PRIMARY KEY (comprobante_reng_id),

    CONSTRAINT uk_asiento_linea_secuencia
        UNIQUE (comprobante_id, secuencia),

    CONSTRAINT fk_al_asiento
        FOREIGN KEY (comprobante_id)
        REFERENCES sc_contabilidad.comprobante(comprobante_id),

    CONSTRAINT fk_al_cuenta
        FOREIGN KEY (afectable_id)
        REFERENCES sc_contabilidad.cuenta_afectable(afectable_id),

    CONSTRAINT fk_al_moneda_mo -- parche §4.3
        FOREIGN KEY (moneda_id_mo)
        REFERENCES sc_administracion.moneda(moneda_id),

    CONSTRAINT ck_al_importe
        CHECK (monto > 0),

    CONSTRAINT ck_al_tipo_cambio
        CHECK (tipo_cambio IS NULL OR tipo_cambio > 0)
);

CREATE TABLE sc_contabilidad.saldo_cuenta (
    saldo_id                BIGINT GENERATED BY DEFAULT AS IDENTITY,
    periodo_id              BIGINT NOT NULL,
    afectable_id            BIGINT NOT NULL,
    saldo_inicial           NUMERIC(20,6) NOT NULL DEFAULT 0,
    total_debito            NUMERIC(20,6) NOT NULL DEFAULT 0,
    total_credito           NUMERIC(20,6) NOT NULL DEFAULT 0,
    saldo_final             NUMERIC(20,6) NOT NULL DEFAULT 0,
    fecha_actualizacion     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario              VARCHAR(50),
    fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    estacion             VARCHAR(30) NOT NULL,

    CONSTRAINT pk_saldo_cuenta
        PRIMARY KEY (saldo_id),

    CONSTRAINT uk_saldo_periodo_cuenta
        UNIQUE (periodo_id, afectable_id),

    CONSTRAINT fk_saldo_periodo
        FOREIGN KEY (periodo_id)
        REFERENCES sc_contabilidad.periodo_contable(periodo_id),

    CONSTRAINT fk_saldo_cuenta
        FOREIGN KEY (afectable_id)
        REFERENCES sc_contabilidad.cuenta_afectable(afectable_id)
);

-- ============================================================
-- Índices sobre toda FK (parche §4.8 — Postgres no los crea solo)
-- ============================================================

CREATE INDEX ix_certif_persona ON sc_socios.certificado_aportacion (persona_id);

CREATE INDEX ix_dominio_valor_dominio ON sc_administracion.dominio_valor (dominio_id);
CREATE INDEX ix_producto_tasa_producto ON sc_administracion.producto_tasa (producto_id);
CREATE INDEX ix_producto_parametro_producto ON sc_administracion.producto_parametro (producto_id);

CREATE INDEX ix_cuenta_captacion_producto ON sc_captaciones.cuenta_captacion (producto_id);
CREATE INDEX ix_cuenta_titular_cuenta ON sc_captaciones.cuenta_titular (cuenta_captacion_id);
CREATE INDEX ix_cuenta_titular_persona ON sc_captaciones.cuenta_titular (persona_id);
CREATE INDEX ix_movimiento_cuenta ON sc_captaciones.movimiento (cuenta_captacion_id);
CREATE INDEX ix_interes_captacion_cuenta ON sc_captaciones.interes_captacion (cuenta_captacion_id);
CREATE INDEX ix_capitalizacion_interes ON sc_captaciones.interes_capitalizacion (interes_captacion_id);
CREATE INDEX ix_capitalizacion_cuenta ON sc_captaciones.interes_capitalizacion (cuenta_captacion_id);

CREATE INDEX ix_cuenta_padre ON sc_contabilidad.cuenta (cuenta_padre_id);
CREATE INDEX ix_cuenta_clasificacion ON sc_contabilidad.cuenta (clasificacion_id);
CREATE INDEX ix_ca_cuenta ON sc_contabilidad.cuenta_afectable (cuenta_id);
CREATE INDEX ix_ca_moneda ON sc_contabilidad.cuenta_afectable (moneda_id);
CREATE INDEX ix_ca_sucursal ON sc_contabilidad.cuenta_afectable (sucursal_id);
CREATE INDEX ix_ca_centro ON sc_contabilidad.cuenta_afectable (centro_id);
CREATE INDEX ix_ca_producto ON sc_contabilidad.cuenta_afectable (producto_id);
CREATE INDEX ix_esquema_detalle_esquema ON sc_contabilidad.esquema_contable_detalle (esquema_contable_id);
CREATE INDEX ix_esquema_detalle_afectable ON sc_contabilidad.esquema_contable_detalle (afectable_id);
CREATE INDEX ix_operacion_moneda ON sc_contabilidad.operacion_contable (moneda_id);
CREATE INDEX ix_comprobante_periodo ON sc_contabilidad.comprobante (periodo_id);
CREATE INDEX ix_comprobante_operacion ON sc_contabilidad.comprobante (operacion_id);
CREATE INDEX ix_comprobante_reng_comprobante ON sc_contabilidad.comprobante_reng (comprobante_id);
CREATE INDEX ix_comprobante_reng_afectable ON sc_contabilidad.comprobante_reng (afectable_id);
CREATE INDEX ix_comprobante_reng_moneda_mo ON sc_contabilidad.comprobante_reng (moneda_id_mo);
CREATE INDEX ix_saldo_cuenta_periodo ON sc_contabilidad.saldo_cuenta (periodo_id);
CREATE INDEX ix_saldo_cuenta_afectable ON sc_contabilidad.saldo_cuenta (afectable_id);
CREATE INDEX ix_dia_contable_periodo ON sc_contabilidad.dia_contable (periodo_id);

-- ============================================================
-- Validación de partida doble (parche §4.9)
-- Antes de autorizar un comprobante (transición de estado a 'AUT'),
-- exige que la suma de renglones debe (D) sea igual a la de haber (H).
-- ============================================================

CREATE OR REPLACE FUNCTION sc_contabilidad.fn_validar_partida_doble()
RETURNS TRIGGER AS $$
DECLARE
    v_debe  NUMERIC(20,6);
    v_haber NUMERIC(20,6);
BEGIN
    SELECT
        COALESCE(SUM(monto) FILTER (WHERE dom_debe_haber = 'D'), 0),
        COALESCE(SUM(monto) FILTER (WHERE dom_debe_haber = 'H'), 0)
    INTO v_debe, v_haber
    FROM sc_contabilidad.comprobante_reng
    WHERE comprobante_id = NEW.comprobante_id;

    IF v_debe <> v_haber THEN
        RAISE EXCEPTION 'Comprobante % no cuadra: debe=% haber=%', NEW.comprobante_id, v_debe, v_haber;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_comprobante_partida_doble
    BEFORE UPDATE OF estado ON sc_contabilidad.comprobante
    FOR EACH ROW
    WHEN (NEW.estado = 'AUT' AND OLD.estado IS DISTINCT FROM 'AUT')
    EXECUTE FUNCTION sc_contabilidad.fn_validar_partida_doble();

-- ============================================================
-- Dominios y valores (dominio.sql) — parche §4.1: esquema/columna corregidos
-- (los INSERT originales apuntaban a sc_captaciones y usaban id_dominio,
-- pero dominio/dominio_valor viven en sc_administracion con columna dominio_id)
-- ============================================================

INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado', 'Estado de registros', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_producto', 'Tipo de productos', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_tasa', 'Tipo de tasa', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_dias_base', 'Días base', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_dato', 'Tipo de dato de parámetro', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_cuenta', 'Tipo de cuenta', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_cuenta', 'Estado de cuenta', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_titular', 'Tipo de titular de cuenta', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_movimiento', 'Tipo de movimiento', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_mov', 'Estado movimiento', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_int', 'Estado de intereses captación', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_mov_int', 'Estado de intereses captación', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_cap', 'Estado de capitalización de interés', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_verif', 'Estado de verificación', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_periodo', 'Estado de periodo o día contable', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_cuenta', 'Tipo de cuenta', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_naturaleza', 'Cuenta deudora o acreedora', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_oper', 'Estado de operación contable', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'V', 'Vigente', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'S', 'Suspendido', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'E', 'Eliminado', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_producto', 'AH', 'Ahorro', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_producto', 'CA', 'Certificado de aportación', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_tasa', 'N', 'Nominal', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_tasa', 'E', 'Efectiva', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_dias_base', '360', '360 días', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_dias_base', '365', '365 días', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'TXT', 'Texto', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'NUM', 'Número sin decimales', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DEC', 'Número con decimales', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DAT', 'Fecha', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DOM', 'Dominio', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_cuenta', 'IND', 'Individual', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_cuenta', 'MNC', 'Mancomunada', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'A', 'Activa', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'C', 'Cerrada', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'B', 'Bloqueada', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'I', 'Inactiva', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_titular', 'P', 'Principal', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_titular', 'S', 'Cotitular', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'DEP', 'Deposito', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'RET', 'Retiro', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'INT', 'Capitalizacion interés', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'AJU', 'Ajuste', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'REG', 'Registrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'PND', 'Pendiente', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'AUT', 'Autorizado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'RZD', 'Rechazado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'REV', 'Revertido', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'REG', 'Registrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'DEV', 'Devengado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'CAP', 'Capitalizado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'REV', 'Revertido', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'REG', 'Registrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'PND', 'Pendiente', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'AUT', 'Autorizado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'RZD', 'Rechazado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'REV', 'Revertido', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_verif', 'PND', 'Pendiente de verificacion', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_verif', 'VRF', 'Verificado', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'A', 'Abierto', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'C', 'Cerrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'R', 'Re abierto', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_cuenta', 'AGR', 'Agrupador', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_cuenta', 'MAY', 'Mayor', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_naturaleza', 'D', 'Deudora', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_naturaleza', 'H', 'Acreedora', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'REG', 'Registrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'PND', 'Pendiente', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'AUT', 'Autorizado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'REV', 'Revertido', 'V', 'sistema', now(), 'init');
