-- ============================================================
-- V17 — Dominios de persona y de certificado de aportación
-- (Observaciones #3 y #10: estados y catálogos parametrizados
--  en lugar de valores quemados en el código).
-- ============================================================

INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_genero',        'Género',                              'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_civil',  'Estado civil de persona',             'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio (dominio_id, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'Estado de certificado de aportación', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_genero', 'F', 'Femenino',  'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_genero', 'M', 'Masculino', 'V', 'sistema', now(), 'init');

INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'S', 'Soltero(a)',    'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'C', 'Casado(a)',     'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'D', 'Divorciado(a)', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'V', 'Viudo(a)',      'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'U', 'Conviviente',   'V', 'sistema', now(), 'init');

-- Flujo de emisión (Obs. #10): REG → PND (aprobado) → VIG (autorizado/vigente) → CRR (retirado)
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'REG', 'Registrado', 'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'PND', 'Aprobado',   'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'VIG', 'Vigente',    'V', 'sistema', now(), 'init');
INSERT INTO sc_administracion.dominio_valor (dominio_id, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'CRR', 'Retirado',   'V', 'sistema', now(), 'init');
