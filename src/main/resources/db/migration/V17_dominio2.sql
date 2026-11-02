INSERT INTO sc_administracion.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_genero', 'Genero', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administracion.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_civil', 'Estado civil de persona', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administracion.dominio (id_dominio, descripcion, estado, usuario, fecha_hora, estacion) VALUES ('dom_estado_aporte', 'Estado de certificado de aportacion', 'V', user, now(), '127.0.0.1');

INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_genero', 'F', 'Femenino', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_genero', 'M', 'Masculino', 'V', user, now(), '127.0.0.1');

INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_civil', 'C', 'Casado', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_civil', 'S', 'Soltero', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_civil', 'D', 'Divorciado', 'V', user, now(), '127.0.0.1');


INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_aporte', 'REG', 'Registrado', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_aporte', 'PND', 'Aprobado', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_aporte', 'VIG', 'Vigente', 'V', user, now(), '127.0.0.1');
INSERT INTO sc_administraciondominio_valor (id_dominio, valor, descripcion_valor, estado, cod_usuario, fecha_hora, estacion) VALUES ( 'dom_estado_aporte', 'CRR', 'Retirado', 'V', user, now(), '127.0.0.1');