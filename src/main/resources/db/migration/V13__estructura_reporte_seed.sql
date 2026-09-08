-- Estructura del Estado de Situación Financiera (ESF) y del Estado de Resultados
-- (ER) — RF-CON-014 / RF-CON-015. V7 creó las tablas y sembró las dos cabeceras;
-- esto siembra sus líneas y el mapeo línea → cuenta contra el plan de V10.
--
-- Cómo se calcula cada línea (lo hace ReporteService):
--  · DETALLE  = Σ del saldo (ya normalizado por naturaleza) de las cuentas
--               mapeadas, con el signo del mapeo. Una cuenta agrupadora suma
--               todas sus cuentas de mayor descendientes.
--  · SUBTOTAL / TOTAL = evalúa `formula`, que referencia códigos de otras líneas.
--  · TITULO   = sin importe (es un encabezado).

-- ============================================================
-- ESF — Estado de Situación Financiera
-- ============================================================

INSERT INTO sc_contabilidad.estructura_reporte_linea
    (estructura_reporte_id, codigo, titulo, orden, tipo_linea, formula, estacion)
SELECT r.estructura_reporte_id, l.codigo, l.titulo, l.orden, l.tipo_linea, l.formula, 'init'
FROM sc_contabilidad.estructura_reporte r
JOIN (VALUES
    ('ESF_ACT',     'ACTIVO',                        1,  'TITULO',   NULL),
    ('ESF_DISP',    'Disponibilidades',              2,  'DETALLE',  NULL),
    ('ESF_CART',    'Cartera de créditos',           3,  'DETALLE',  NULL),
    ('ESF_OCC',     'Otras cuentas por cobrar',      4,  'DETALLE',  NULL),
    ('ESF_TOT_ACT', 'Total activo',                  5,  'SUBTOTAL', 'ESF_DISP + ESF_CART + ESF_OCC'),
    ('ESF_PAS',     'PASIVO',                        6,  'TITULO',   NULL),
    ('ESF_OBL',     'Obligaciones con el público',   7,  'DETALLE',  NULL),
    ('ESF_OCP',     'Otras cuentas por pagar',       8,  'DETALLE',  NULL),
    ('ESF_TOT_PAS', 'Total pasivo',                  9,  'SUBTOTAL', 'ESF_OBL + ESF_OCP'),
    ('ESF_PAT',     'PATRIMONIO',                    10, 'TITULO',   NULL),
    ('ESF_CAP',     'Capital social',                11, 'DETALLE',  NULL),
    ('ESF_RAC',     'Resultados acumulados',         12, 'DETALLE',  NULL),
    ('ESF_RES',     'Resultado del ejercicio',       13, 'DETALLE',  NULL),
    ('ESF_TOT_PAT', 'Total patrimonio',              14, 'SUBTOTAL', 'ESF_CAP + ESF_RAC + ESF_RES'),
    ('ESF_TOT_PP',  'Total pasivo y patrimonio',     15, 'TOTAL',    'ESF_TOT_PAS + ESF_TOT_PAT')
) AS l(codigo, titulo, orden, tipo_linea, formula) ON TRUE
WHERE r.codigo = 'ESF';

-- mapeo línea → cuenta (por código de cuenta; una agrupadora arrastra sus mayores)
INSERT INTO sc_contabilidad.estructura_reporte_cuenta
    (estructura_reporte_linea_id, cuenta_id, signo, estacion)
SELECT erl.estructura_reporte_linea_id, c.cuenta_id, m.signo, 'init'
FROM (VALUES
    ('ESF_DISP', '11',  1),
    ('ESF_CART', '13',  1),
    ('ESF_OCC',  '16',  1),
    ('ESF_OBL',  '21',  1),
    ('ESF_OCP',  '25',  1),
    ('ESF_CAP',  '31',  1),
    ('ESF_RAC',  '34',  1),
    ('ESF_RES',  '4',   1),
    ('ESF_RES',  '5',  -1)
) AS m(linea_codigo, cuenta_codigo, signo)
JOIN sc_contabilidad.estructura_reporte r ON r.codigo = 'ESF'
JOIN sc_contabilidad.estructura_reporte_linea erl
    ON erl.estructura_reporte_id = r.estructura_reporte_id AND erl.codigo = m.linea_codigo
JOIN sc_contabilidad.cuenta c ON c.codigo = m.cuenta_codigo;

-- ============================================================
-- ER — Estado de Resultados
-- ============================================================

INSERT INTO sc_contabilidad.estructura_reporte_linea
    (estructura_reporte_id, codigo, titulo, orden, tipo_linea, formula, estacion)
SELECT r.estructura_reporte_id, l.codigo, l.titulo, l.orden, l.tipo_linea, l.formula, 'init'
FROM sc_contabilidad.estructura_reporte r
JOIN (VALUES
    ('ER_ING',     'INGRESOS',                 1, 'TITULO',   NULL),
    ('ER_INGF',    'Ingresos financieros',     2, 'DETALLE',  NULL),
    ('ER_TOT_ING', 'Total ingresos',           3, 'SUBTOTAL', 'ER_INGF'),
    ('ER_GAS',     'GASTOS',                   4, 'TITULO',   NULL),
    ('ER_GASF',    'Gastos financieros',       5, 'DETALLE',  NULL),
    ('ER_GASA',    'Gastos de administración', 6, 'DETALLE',  NULL),
    ('ER_TOT_GAS', 'Total gastos',             7, 'SUBTOTAL', 'ER_GASF + ER_GASA'),
    ('ER_RES',     'Resultado del ejercicio',  8, 'TOTAL',    'ER_TOT_ING - ER_TOT_GAS')
) AS l(codigo, titulo, orden, tipo_linea, formula) ON TRUE
WHERE r.codigo = 'ER';

INSERT INTO sc_contabilidad.estructura_reporte_cuenta
    (estructura_reporte_linea_id, cuenta_id, signo, estacion)
SELECT erl.estructura_reporte_linea_id, c.cuenta_id, 1, 'init'
FROM (VALUES
    ('ER_INGF', '41'),
    ('ER_GASF', '51'),
    ('ER_GASA', '52')
) AS m(linea_codigo, cuenta_codigo)
JOIN sc_contabilidad.estructura_reporte r ON r.codigo = 'ER'
JOIN sc_contabilidad.estructura_reporte_linea erl
    ON erl.estructura_reporte_id = r.estructura_reporte_id AND erl.codigo = m.linea_codigo
JOIN sc_contabilidad.cuenta c ON c.codigo = m.cuenta_codigo;
