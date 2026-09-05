--Administracion de socios

--drop table sc_socios.certificado_aportacion;
--drop table sc_socios.persona;


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
        numero_telefono      VARCHAR(10) NOT NULL, 
        fecha_nacimiento     DATE        NOT NULL, 
        estado_civil         VARCHAR(20) NOT NULL, 
        estado               VARCHAR(3) NOT NULL, 
        usuario              VARCHAR(50), 
        fecha_hora           TIMESTAMP(6) WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP, 
        estacion             VARCHAR(30) NOT NULL,
        CONSTRAINT pk_socio PRIMARY KEY (persona_id) 
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