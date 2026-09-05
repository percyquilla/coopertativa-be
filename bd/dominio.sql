INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado', 'Estado de registros', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_producto', 'Tipo de productos', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_tasa', 'Tipo de tasa', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_dias_base', 'Días base', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_dato', 'Tipo de dato de parámetro', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_cuenta', 'Tipo de cuenta', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_cuenta', 'Estado de cuenta', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_titular', 'Tipo de titular de cuenta', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_movimiento', 'Tipo de movimiento', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_mov', 'Estado movimiento', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_int', 'Estado de intereres captacion', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_mov_int', 'Estado de intereres captacion', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_cap', 'Estado de capitalización de interes', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_verif', 'Estado de verificacion', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_periodo', 'Estado de periodo o día contable', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_tipo_cuenta', 'Tipo de cuenta', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_naturaleza', 'Cuenta deudora o acreedora', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_captaciones.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_oper', 'Cuenta deudora o acreedora', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'V', 'Vigente', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'S', 'Suspendido', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado', 'E', 'Eliminado', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_producto', 'AH', 'Ahorro', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_producto', 'CA', 'Certificado de aportación', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_tasa', 'N', 'Nominal', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_tasa', 'E', 'Efectiva', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_dias_base', '360', '360 días', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_dias_base', '365', '365 días', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'TXT', 'Texto', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'NUM', 'Número sin decimales', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DEC', 'Número con decimales', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DAT', 'Fecha', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_dato', 'DOM', 'Dominio', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_cuenta', 'IND', 'Individual', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_cuenta', 'MNC', 'Mancomunada', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'A', 'Activa', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'C', 'Cerrada', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'B', 'Bloqueada', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cuenta', 'I', 'Inactiva', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_titular', 'P', 'Principal', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_titular', 'S', 'Cotitular', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'DEP', 'Deposito', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'RET', 'Retiro', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'INT', 'Capitalizacion interés', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_movimiento', 'AJU', 'Ajuste', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'REG', 'Registrado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'PND', 'Pendiente', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'AUT', 'Autorizado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'RZD', 'Rechazado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_mov', 'REV', 'Revertido', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'REG', 'Registrado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'DEV', 'Devengado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'CAP', 'Capitalizado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_int', 'REV', 'Revetido', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'REG', 'Registrado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'PND', 'Pendiente', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'AUT', 'Autorizado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'RZD', 'Rechazado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_cap', 'REV', 'Revertido', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_verif', 'PND', 'Pendiente de verificacion', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_verif', 'VRF', 'Verificado', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'A', 'Abierto', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'C', 'Cerrado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_periodo', 'R', 'Re abierto', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_cuenta', 'AGR', 'Agrupador', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_tipo_cuenta', 'MAY', 'Mayor', 'V', user, now(), '127.0.0.1');

INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_naturaleza', 'D', 'Deudora', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_naturaleza', 'H', 'Acreedora', 'V', user, now(), '127.0.0.1');


INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'REG', 'Registrado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'PND', 'Pendiente', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'AUT', 'Autorizado', 'V', user, now(), '127.0.0.1');
INSERT INTO dominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_oper', 'REV', 'Revertido', 'V', user, now(), '127.0.0.1');