-- ============================================================
-- V18 — Flujo de emisión de certificados y de solicitudes de retiro
-- (Observaciones #8, #10 y #11)
--
--   Certificado de aportación:  REG → PND (aprobado) → VIG (autorizado) → CRR (retirado)
--                               REG/PND → ANL (anulado)
--   Solicitud de retiro:        REG → PND (aprobada) → AUT (autorizada)
--                               REG/PND → RCH (rechazada) | ANL (anulada)
--
-- Al autorizar se genera la operación contable (APO_EMI / APO_DEV) y su id
-- queda en la columna operacion_id (agregada en V16).
-- ============================================================

-- ---- Parámetro: valor unitario por defecto del certificado (Obs. #8) ----
INSERT INTO sc_parametros.parametro_general (codigo, nombre, descripcion, valor, tipo_dato, editable, estacion) VALUES
    ('VALOR_UNIT_APORTE', 'Valor unitario de aportación',
     'Valor nominal por defecto de cada certificado de aportación emitido.', '500.00', 'NUM', TRUE, 'init');

-- ---- Dominios ----
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'ANL', 'Anulado', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'Estado de solicitud de retiro', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'REG', 'Registrada', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'PND', 'Aprobada',   'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'AUT', 'Autorizada', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'RCH', 'Rechazada',  'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'ANL', 'Anulada',    'V', 'sistema', now(), 'init');

-- ---- Datos existentes: estados del flujo anterior al nuevo ----
-- Certificado: V (vigente) → VIG, A (anulado) → ANL.
UPDATE sc_socios.certificado_aportacion SET estado = 'VIG' WHERE estado = 'V';
UPDATE sc_socios.certificado_aportacion SET estado = 'ANL' WHERE estado = 'A';
ALTER TABLE sc_socios.certificado_aportacion ALTER COLUMN estado SET DEFAULT 'REG';

-- Solicitud: PND (recién registrada) → REG, APR → PND, PGD (pagada) → AUT.
UPDATE sc_socios.solicitud_retiro SET estado = 'REG' WHERE estado = 'PND';
UPDATE sc_socios.solicitud_retiro SET estado = 'PND' WHERE estado = 'APR';
UPDATE sc_socios.solicitud_retiro SET estado = 'AUT' WHERE estado = 'PGD';
ALTER TABLE sc_socios.solicitud_retiro ALTER COLUMN estado SET DEFAULT 'REG';

-- ---- Integridad de las columnas de V16 ----
ALTER TABLE sc_socios.certificado_aportacion
    ADD CONSTRAINT fk_certif_operacion
        FOREIGN KEY (operacion_id) REFERENCES sc_contabilidad.operacion_contable (operacion_id);

ALTER TABLE sc_socios.solicitud_retiro
    ADD CONSTRAINT fk_solretiro_operacion
        FOREIGN KEY (operacion_id) REFERENCES sc_contabilidad.operacion_contable (operacion_id),
    ADD CONSTRAINT fk_solretiro_certificado
        FOREIGN KEY (certificado_id) REFERENCES sc_socios.certificado_aportacion (certificado_id);

CREATE INDEX ix_solretiro_certificado ON sc_socios.solicitud_retiro (certificado_id);
