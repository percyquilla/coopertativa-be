-- Fase 6 — Intereses de caja de ahorro.
--  · devengo diario  -> asiento DEV_INT  (D 5110 Gasto intereses / H 2130 Intereses por pagar)
--  · capitalización mensual -> asiento CAP_INT (D 2130 / H 2110 Cajas de ahorro) + movimiento INT
-- Las afectables ya existen (V10): 5110-BOB, 2130-BOB, 2110-BOB.

-- ============================================================
-- 1. Índice para buscar el devengo del mes de una cuenta
-- ============================================================
CREATE INDEX ix_interes_captacion_cuenta_estado
    ON sc_captaciones.interes_captacion (cuenta_captacion_id, estado);

-- ============================================================
-- 2. Esquemas contables DEV_INT / CAP_INT (versión 1, vigentes)
-- ============================================================
INSERT INTO sc_contabilidad.esquema_contable
    (codigo, nombre, version, estado, descripcion, estacion)
VALUES
    ('DEV_INT', 'Devengo de intereses de caja de ahorro',        1, 'V',
        'Debita el gasto por intereses, acredita la obligación por pagar.', 'init'),
    ('CAP_INT', 'Capitalización de intereses de caja de ahorro',  1, 'V',
        'Debita la obligación por pagar, acredita el depósito del socio.',  'init');

INSERT INTO sc_contabilidad.esquema_contable_detalle
    (esquema_contable_id, secuencia, dom_debe_haber, afectable_id, formula, descripcion)
SELECT e.esquema_contable_id, d.secuencia, d.dh, a.afectable_id, 'MONTO', d.descripcion
FROM (VALUES
    ('DEV_INT', 1, 'D', '5110-BOB', 'Gasto por intereses del día'),
    ('DEV_INT', 2, 'H', '2130-BOB', 'Intereses por pagar al socio'),
    ('CAP_INT', 1, 'D', '2130-BOB', 'Cancelación de la obligación por intereses'),
    ('CAP_INT', 2, 'H', '2110-BOB', 'Aumento del depósito del socio por capitalización')
) AS d(esquema_codigo, secuencia, dh, afectable_codigo, descripcion)
JOIN sc_contabilidad.esquema_contable e
    ON e.codigo = d.esquema_codigo AND e.version = 1
JOIN sc_contabilidad.cuenta_afectable a
    ON a.codigo = d.afectable_codigo;

-- ============================================================
-- 3. Tipos de operación, ligados a su esquema y al comprobante de diario
-- ============================================================
INSERT INTO sc_administracion.tipo_operacion
    (codigo, descripcion, estado, esquema_contable_id, tipo_comprobante_id, estacion)
SELECT t.codigo, t.descripcion, 'V', e.esquema_contable_id, tc.tipo_comprobante_id, 'init'
FROM (VALUES
    ('DEV_INT', 'Devengo de intereses de caja de ahorro'),
    ('CAP_INT', 'Capitalización de intereses de caja de ahorro')
) AS t(codigo, descripcion)
JOIN sc_contabilidad.esquema_contable e
    ON e.codigo = t.codigo AND e.version = 1
JOIN sc_contabilidad.tipo_comprobante tc
    ON tc.codigo = 'DIA';

-- ============================================================
-- 4. Tasa de ejemplo para el producto AH01 (caja de ahorro):
--    2,5 % anual nominal, base 365. Editable desde la pantalla de Tasas.
-- ============================================================
INSERT INTO sc_administracion.producto_tasa
    (producto_id, tasa, tipo_tasa, base_dias, fecha_inicio, estado, estacion)
SELECT p.producto_id, 2.500000, 'N', 365, DATE '2026-01-01', 'V', 'init'
FROM sc_administracion.producto p
WHERE p.codigo = 'AH01'
  AND NOT EXISTS (
        SELECT 1 FROM sc_administracion.producto_tasa pt WHERE pt.producto_id = p.producto_id
  );
