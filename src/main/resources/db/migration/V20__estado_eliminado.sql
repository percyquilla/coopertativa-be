-- ============================================================
-- V20 — Eliminación lógica (ELM) de certificados y solicitudes de retiro
--
-- "Eliminar" ya no borra la fila: la deja en estado ELM, con usuario,
-- fecha/hora y estación de quien la eliminó. Los registros ELM no se listan.
-- Solo se elimina un registro en REG o RZD (todavía sin efecto contable).
-- ============================================================

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'ELM', 'Eliminado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_retiro', 'ELM', 'Eliminada', 'V', 'sistema', now(), 'init');
