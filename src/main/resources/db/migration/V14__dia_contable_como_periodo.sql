-- El día contable pasa a comportarse como el período contable:
--   * estados 'P' pendiente de apertura · 'A' abierto · 'C' cerrado · 'R' reabierto
--   * toda fecha de un período no cerrado tiene su fila (sin fila = no se puede asentar)
--   * columnas de auditoría, igual que periodo_contable
-- (V15 no hace falta: 'estado' ya es VARCHAR(20), 'R'/'P' entran)

ALTER TABLE sc_contabilidad.dia_contable
    ADD COLUMN usuario    VARCHAR(50),
    ADD COLUMN estacion   VARCHAR(30) NOT NULL DEFAULT 'SISTEMA',
    ADD COLUMN fecha_hora  TIMESTAMP  NOT NULL DEFAULT now();

-- Pre-generar los días de cada período NO cerrado. Los períodos cerrados ya
-- bloquean por sí mismos (findAbiertoPorFechaOrThrow), no necesitan filas.
--   fecha < hoy  -> 'C' (días pasados: se consideran cerrados, frontera limpia)
--   fecha >= hoy -> 'P' (pendientes de apertura)
-- Las filas que ya existen (uk_dia_contable es sobre 'fecha') se respetan.
INSERT INTO sc_contabilidad.dia_contable (fecha, periodo_id, estado, usuario, estacion)
SELECT g.d::date,
       p.periodo_id,
       CASE WHEN g.d::date < CURRENT_DATE THEN 'C' ELSE 'P' END,
       'sistema',
       'V14'
FROM sc_contabilidad.periodo_contable p
CROSS JOIN LATERAL generate_series(p.fecha_inicio, p.fecha_fin, INTERVAL '1 day') AS g(d)
WHERE p.estado <> 'C'
  AND NOT EXISTS (
        SELECT 1 FROM sc_contabilidad.dia_contable x WHERE x.fecha = g.d::date
  );
