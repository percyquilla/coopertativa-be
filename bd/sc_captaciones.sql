

drop table sc_captaciones.cuenta_titular;
drop table sc_captaciones.dominio_valor;
drop table sc_captaciones.dominio;
drop table sc_captaciones.interes_capitalizacion;
drop table sc_captaciones.interes_captacion;
drop table sc_captaciones.movimiento_captacion;
drop table sc_captaciones.cuenta_captacion;
drop table sc_captaciones.producto_captacion_parametro;
drop table sc_captaciones.producto_captacion_tasa;
drop table sc_captaciones.producto_captacion;
   
    
--Captaciones 
    
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