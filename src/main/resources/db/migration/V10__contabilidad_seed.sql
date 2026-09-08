-- Semilla de contabilidad: clasificaciones, plan de cuentas, centro, períodos
-- y cuentas afectables (CONTEXTO-BD.md §5.3 / §7 Paso 7).
--
-- V1 creó las tablas de sc_contabilidad vacías y V7 solo sembró
-- tipo_comprobante. Sin un plan de cuentas no hay nada a lo que apuntar:
-- ni cuenta_afectable, ni esquema_contable_detalle, ni comprobante_reng.
--
-- Las cuentas usan dom_tipo_cuenta: 'AGR' (agrupador, no imputable) y 'MAY'
-- (mayor, imputable). Solo las MAY tienen cuenta_afectable.

-- ============================================================
-- 1. Clasificaciones (naturaleza deudora/acreedora)
-- ============================================================

INSERT INTO sc_contabilidad.clasificacion_cuenta (codigo, dom_debe_haber, descripcion, estacion) VALUES
    ('ACT', 'D', 'Activo',      'init'),
    ('PAS', 'H', 'Pasivo',      'init'),
    ('PAT', 'H', 'Patrimonio',  'init'),
    ('ING', 'H', 'Ingresos',    'init'),
    ('GAS', 'D', 'Gastos',      'init');

-- ============================================================
-- 2. Plan de cuentas, nivel por nivel
-- (el padre se resuelve por código para no fijar ids a mano)
-- ============================================================

-- Nivel 1 — grupos
INSERT INTO sc_contabilidad.cuenta
    (cuenta_padre_id, clasificacion_id, dom_tipo_cuenta, codigo, nombre, nivel, estacion)
SELECT NULL, c.clasificacion_id, 'AGR', v.codigo, v.nombre, 1, 'init'
FROM (VALUES
    ('1', 'Activo',     'ACT'),
    ('2', 'Pasivo',     'PAS'),
    ('3', 'Patrimonio', 'PAT'),
    ('4', 'Ingresos',   'ING'),
    ('5', 'Gastos',     'GAS')
) AS v(codigo, nombre, clas)
JOIN sc_contabilidad.clasificacion_cuenta c ON c.codigo = v.clas;

-- Nivel 2 — rubros
INSERT INTO sc_contabilidad.cuenta
    (cuenta_padre_id, clasificacion_id, dom_tipo_cuenta, codigo, nombre, nivel, estacion)
SELECT p.cuenta_id, c.clasificacion_id, 'AGR', v.codigo, v.nombre, 2, 'init'
FROM (VALUES
    ('11', 'Disponibilidades',            'ACT', '1'),
    ('13', 'Cartera de créditos',         'ACT', '1'),
    ('16', 'Otras cuentas por cobrar',    'ACT', '1'),
    ('21', 'Obligaciones con el público', 'PAS', '2'),
    ('25', 'Otras cuentas por pagar',     'PAS', '2'),
    ('31', 'Capital social',              'PAT', '3'),
    ('34', 'Resultados acumulados',       'PAT', '3'),
    ('41', 'Ingresos financieros',        'ING', '4'),
    ('51', 'Gastos financieros',          'GAS', '5'),
    ('52', 'Gastos de administración',    'GAS', '5')
) AS v(codigo, nombre, clas, padre)
JOIN sc_contabilidad.clasificacion_cuenta c ON c.codigo = v.clas
JOIN sc_contabilidad.cuenta p ON p.codigo = v.padre;

-- Nivel 3 — subrubros
INSERT INTO sc_contabilidad.cuenta
    (cuenta_padre_id, clasificacion_id, dom_tipo_cuenta, codigo, nombre, nivel, estacion)
SELECT p.cuenta_id, c.clasificacion_id, 'AGR', v.codigo, v.nombre, 3, 'init'
FROM (VALUES
    ('111', 'Caja',                                    'ACT', '11'),
    ('112', 'Bancos',                                  'ACT', '11'),
    ('161', 'Intereses devengados por cobrar',         'ACT', '16'),
    ('211', 'Depósitos a la vista',                    'PAS', '21'),
    ('212', 'Depósitos a plazo fijo',                  'PAS', '21'),
    ('213', 'Intereses por pagar',                     'PAS', '21'),
    ('251', 'Acreedores varios',                       'PAS', '25'),
    ('311', 'Certificados de aportación',              'PAT', '31'),
    ('341', 'Resultados de gestiones anteriores',      'PAT', '34'),
    ('411', 'Intereses ganados',                       'ING', '41'),
    ('412', 'Comisiones ganadas',                      'ING', '41'),
    ('511', 'Intereses por obligaciones con el público', 'GAS', '51'),
    ('521', 'Gastos de personal',                      'GAS', '52'),
    ('522', 'Servicios contratados',                   'GAS', '52')
) AS v(codigo, nombre, clas, padre)
JOIN sc_contabilidad.clasificacion_cuenta c ON c.codigo = v.clas
JOIN sc_contabilidad.cuenta p ON p.codigo = v.padre;

-- Nivel 4 — cuentas de mayor (las únicas imputables)
INSERT INTO sc_contabilidad.cuenta
    (cuenta_padre_id, clasificacion_id, dom_tipo_cuenta, codigo, nombre, nivel, estacion)
SELECT p.cuenta_id, c.clasificacion_id, 'MAY', v.codigo, v.nombre, 4, 'init'
FROM (VALUES
    ('1110', 'Caja moneda nacional',                'ACT', '111'),
    ('1111', 'Caja moneda extranjera',              'ACT', '111'),
    ('1120', 'Banco cuenta corriente MN',           'ACT', '112'),
    ('1610', 'Intereses por cobrar cartera',        'ACT', '161'),
    ('2110', 'Cajas de ahorro',                     'PAS', '211'),
    ('2120', 'Depósitos a plazo fijo MN',           'PAS', '212'),
    ('2130', 'Intereses por pagar cajas de ahorro', 'PAS', '213'),
    ('2510', 'Acreedores varios',                   'PAS', '251'),
    ('3110', 'Aportaciones de socios',              'PAT', '311'),
    ('3410', 'Resultados acumulados',               'PAT', '341'),
    ('4110', 'Intereses ganados de cartera',        'ING', '411'),
    ('4120', 'Comisiones por servicios',            'ING', '412'),
    ('5110', 'Intereses cajas de ahorro',           'GAS', '511'),
    ('5120', 'Intereses depósitos a plazo fijo',    'GAS', '511'),
    ('5210', 'Sueldos y salarios',                  'GAS', '521'),
    ('5220', 'Servicios profesionales',             'GAS', '522')
) AS v(codigo, nombre, clas, padre)
JOIN sc_contabilidad.clasificacion_cuenta c ON c.codigo = v.clas
JOIN sc_contabilidad.cuenta p ON p.codigo = v.padre;

-- ============================================================
-- 3. Centro por defecto
-- ============================================================

INSERT INTO sc_contabilidad.centro (codigo, nombre, estacion) VALUES
    ('GEN', 'General', 'init');

-- ============================================================
-- 4. Períodos de la gestión 2026
-- Ene–Ago cerrados, Sep–Dic abiertos (la fecha de trabajo es 2026-09).
-- ============================================================

INSERT INTO sc_contabilidad.periodo_contable (gestion, mes, fecha_inicio, fecha_fin, estado, estacion)
SELECT
    2026,
    m,
    make_date(2026, m, 1),
    (make_date(2026, m, 1) + INTERVAL '1 month - 1 day')::date,
    CASE WHEN m < 9 THEN 'C' ELSE 'A' END,
    'init'
FROM generate_series(1, 12) AS m;

-- ============================================================
-- 5. Cuentas afectables — una por cuenta de mayor, en la moneda base
-- ============================================================

INSERT INTO sc_contabilidad.cuenta_afectable
    (cuenta_id, codigo, moneda_id, centro_id, descripcion, estacion)
SELECT
    c.cuenta_id,
    c.codigo || '-BOB',
    (SELECT moneda_id FROM sc_administracion.moneda WHERE codigo = 'BOB'),
    (SELECT centro_id FROM sc_contabilidad.centro WHERE codigo = 'GEN'),
    c.nombre || ' (BOB)',
    'init'
FROM sc_contabilidad.cuenta c
WHERE c.dom_tipo_cuenta = 'MAY';
