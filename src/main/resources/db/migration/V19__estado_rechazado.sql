-- ============================================================
-- V19 — Estado "rechazado" (RZD) y fin de la anulación
--
-- Certificado de aportación y solicitud de retiro siguen el mismo flujo:
--   REG → aprobar → PND → autorizar → VIG / AUT
--   REG → eliminar
--   PND → rechazar → RZD → modificar → REG, o eliminar
-- La anulación (ANL) deja de existir; más adelante se habilitará "revertir".
-- ============================================================

-- Certificado: nuevo estado RZD; ANL queda como valor no vigente del catálogo.
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'RZD', 'Rechazado', 'V', 'sistema', now(), 'init');
UPDATE sc_administracion.dominio_valor SET estado = 'S' WHERE dominio_id = 'dom_estado_aporte' AND valor = 'ANL';

-- Solicitud de retiro: RCH → RZD; ANL queda como valor no vigente.
UPDATE sc_administracion.dominio_valor SET valor = 'RZD' WHERE dominio_id = 'dom_estado_retiro' AND valor = 'RCH';
UPDATE sc_administracion.dominio_valor SET estado = 'S' WHERE dominio_id = 'dom_estado_retiro' AND valor = 'ANL';

UPDATE sc_socios.solicitud_retiro SET estado = 'RZD' WHERE estado = 'RCH';
