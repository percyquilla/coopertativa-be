-- Aportaciones como producto de captación tipo CA (CONTEXTO-BD.md §5.1 / §7 Paso 6).
--
-- Decisión de diseño (§5.1): las aportaciones se manejan como una
-- `cuenta_captacion` con producto de tipo 'CA'. Así se reutilizan saldo,
-- movimientos, intereses y contabilización. `certificado_aportacion` queda
-- solo como el título nominal (número, valor unitario, cantidad) y apunta a
-- la cuenta que lleva la mecánica.
--
-- Sin Java: el CRUD de captaciones (cuenta_captacion, movimiento,
-- certificado_aportacion) es una construcción aparte.

-- ============================================================
-- Producto CA semilla
-- ============================================================

INSERT INTO sc_administracion.producto
    (codigo, nombre, descripcion, tipo_producto, moneda, saldo_minimo,
     permite_deposito, permite_retiro, permite_interes, estacion)
VALUES
    ('CA', 'Certificado de aportación',
     'Aportaciones de capital de los socios. Se opera como cuenta_captacion de tipo_producto CA; el retiro solo procede por solicitud_retiro.',
     'CA', 'BOB', 0, TRUE, FALSE, TRUE, 'init');

-- ============================================================
-- certificado_aportacion → título nominal
-- (V1 lo creó copiado del diseño base con persona_id/monto_estado; acá se
-- reconvierte. La tabla está vacía, así que se reescriben las columnas.)
-- ============================================================

ALTER TABLE sc_socios.certificado_aportacion
    DROP CONSTRAINT fk_certif_persona,
    DROP COLUMN persona_id,
    DROP COLUMN monto_estado,
    DROP COLUMN fecha,
    ADD COLUMN socio_id            BIGINT        NOT NULL,
    ADD COLUMN cuenta_captacion_id BIGINT,
    ADD COLUMN numero              VARCHAR(30)   NOT NULL,
    ADD COLUMN valor_unitario      NUMERIC(18,2) NOT NULL,
    ADD COLUMN cantidad            INTEGER       NOT NULL,
    ADD COLUMN fecha_emision       DATE          NOT NULL;

ALTER TABLE sc_socios.certificado_aportacion
    ALTER COLUMN estado SET DEFAULT 'V';

ALTER TABLE sc_socios.certificado_aportacion
    ADD CONSTRAINT fk_certif_socio
        FOREIGN KEY (socio_id) REFERENCES sc_socios.socio (socio_id),
    ADD CONSTRAINT fk_certif_cuenta
        FOREIGN KEY (cuenta_captacion_id) REFERENCES sc_captaciones.cuenta_captacion (cuenta_captacion_id),
    ADD CONSTRAINT uk_certif_numero UNIQUE (numero),
    ADD CONSTRAINT ck_certif_valores
        CHECK (valor_unitario > 0 AND cantidad > 0);

CREATE INDEX ix_certif_socio ON sc_socios.certificado_aportacion (socio_id);
CREATE INDEX ix_certif_cuenta ON sc_socios.certificado_aportacion (cuenta_captacion_id);
