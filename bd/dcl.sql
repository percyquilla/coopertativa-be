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

-- Privilegios sobre el schema de seguridad (ejecutar conectado a la BD bienbcb, despues de crear el schema en ddl.sql)
GRANT ALL PRIVILEGES ON SCHEMA sc_seguridad TO bienbcb;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA sc_seguridad TO bienbcb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA sc_seguridad TO bienbcb;

ALTER DEFAULT PRIVILEGES IN SCHEMA sc_seguridad
    GRANT ALL PRIVILEGES ON TABLES TO bienbcb;
ALTER DEFAULT PRIVILEGES IN SCHEMA sc_seguridad
    GRANT ALL PRIVILEGES ON SEQUENCES TO bienbcb;
