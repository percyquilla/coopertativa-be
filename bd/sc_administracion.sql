





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
        dominio_id        VARCHAR(20) NOT NULL, 
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