-- Rol/usuario de aplicacion (ejecutar conectado a una BD existente, p.ej. postgres, antes del ddl.sql)
CREATE ROLE bienbcb WITH
    LOGIN
    PASSWORD 'bienbcb';

-- Privilegios sobre la base de datos (ejecutar despues de crear la BD en ddl.sql)
GRANT ALL PRIVILEGES ON DATABASE bienbcb TO bienbcb;

-- Privilegios sobre el schema (ejecutar conectado a la BD bienbcb, despues de crear el schema en ddl.sql)
GRANT ALL PRIVILEGES ON SCHEMA sc_socios TO bienbcb;

-- Privilegios sobre objetos existentes del schema
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_socios TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_socios TO bienbcb;

-- Privilegios por defecto sobre objetos futuros del schema
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_socios
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_socios
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;

-- Privilegios sobre los schemas del diseño base (ejecutar despues de que Flyway aplique V1__init.sql)
GRANT ALL PRIVILEGES ON SCHEMA sc_administracion TO bienbcb;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_administracion TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_administracion TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_administracion
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_administracion
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;

GRANT ALL PRIVILEGES ON SCHEMA sc_captaciones TO bienbcb;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_captaciones TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_captaciones TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_captaciones
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_captaciones
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;

GRANT ALL PRIVILEGES ON SCHEMA sc_contabilidad TO bienbcb;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_contabilidad TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_contabilidad TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_contabilidad
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_contabilidad
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;

GRANT ALL PRIVILEGES ON SCHEMA sc_parametros TO bienbcb;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_parametros TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_parametros TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_parametros
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_parametros
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;

-- Privilegios sobre el schema de seguridad (ejecutar conectado a la BD bienbcb, despues de crear el schema en ddl.sql)
GRANT ALL PRIVILEGES ON SCHEMA sc_seguridad TO bienbcb;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_seguridad TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_seguridad TO bienbcb;

ALTER DEFAULT PRIVILEGES IN SCHEMA sc_seguridad
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_seguridad
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;
