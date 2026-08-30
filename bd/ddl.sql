-- Base de datos (ejecutar conectado a una BD existente, p.ej. postgres, no puede ir en una transaccion junto al resto del script)
CREATE DATABASE bienbcb
    OWNER bienbcb
    ENCODING 'UTF8';

-- Schema (ejecutar conectado a la BD bienbcb)
CREATE SCHEMA IF NOT EXISTS sc_socios AUTHORIZATION bienbcb;

CREATE TABLE sc_socios.persona (
                                   id_persona BIGINT GENERATED ALWAYS AS IDENTITY,
                                   apellido_paterno VARCHAR(50) NOT NULL,
                                   apellido_materno VARCHAR(50) NOT NULL,
                                   apellido_casada VARCHAR(50),
                                   nombres VARCHAR(60) NOT NULL,
                                   cedula_identidad VARCHAR(20) NOT NULL,
                                   expedicion VARCHAR(5),
                                   genero VARCHAR(1) NOT NULL,
                                   lugar_trabajo VARCHAR(80),
                                   cargo_actual VARCHAR(150),
                                   correo_institucional VARCHAR(80),
                                   correo_personal VARCHAR(80),
                                   numero_telefono VARCHAR(10) NOT NULL,
                                   fecha_nacimiento DATE NOT NULL,
                                   estado_civil VARCHAR(20) NOT NULL,
                                   usuario VARCHAR(50),
                                   fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT pk_socio
                                       PRIMARY KEY (id_persona)
);

-- Schema de seguridad (ejecutar conectado a la BD bienbcb)
CREATE SCHEMA IF NOT EXISTS sc_seguridad AUTHORIZATION bienbcb;

CREATE TABLE sc_seguridad.usuario (
                                   id_usuario BIGINT GENERATED ALWAYS AS IDENTITY,
                                   id_persona BIGINT,
                                   username VARCHAR(50) NOT NULL,
                                   password VARCHAR(100) NOT NULL,
                                   salt VARCHAR(64) NOT NULL,
                                   rol VARCHAR(20) NOT NULL,
                                   activo BOOLEAN NOT NULL DEFAULT TRUE,
                                   fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT pk_usuario
                                       PRIMARY KEY (id_usuario),
                                   CONSTRAINT uq_usuario_username
                                       UNIQUE (username),
                                   CONSTRAINT uq_usuario_persona
                                       UNIQUE (id_persona),
                                   CONSTRAINT fk_usuario_persona
                                       FOREIGN KEY (id_persona) REFERENCES sc_socios.persona (id_persona),
                                   CONSTRAINT ck_usuario_rol
                                       CHECK (rol IN ('ADMIN', 'USER'))
);

-- Usuario semilla (username: admin / password: admin123, hash SHA-256 con salt aleatorio) - cambiar password en produccion
INSERT INTO sc_seguridad.usuario (username, password, salt, rol, activo)
VALUES ('admin', 'f97ba39c47aa876240161aedb9d3c9d44935e148769fc7472d8a06b21a904b0c', '4f06293f8315ffe881ab4447de6b4862', 'ADMIN', TRUE);