alter table sc_socios.certificado_aportacion add (operacion_id BIGINT);

alter table sc_socios.solicitud_retiro add (operacion_id BIGINT);

alter table sc_socios.solicitud_retiro add (certificado_id BIGINT);
