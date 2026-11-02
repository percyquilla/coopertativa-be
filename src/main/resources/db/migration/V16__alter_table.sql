

ALTER TABLE sc_socios.certificado_aportacion ADD COLUMN operacion_id BIGINT;

ALTER TABLE sc_socios.solicitud_retiro ADD COLUMN operacion_id BIGINT;

ALTER TABLE sc_socios.solicitud_retiro ADD COLUMN certificado_id BIGINT;