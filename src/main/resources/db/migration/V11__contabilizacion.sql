-- Motor contabilizador (CONTEXTO-BD.md §6 / §7 Paso 10, la parte de asiento
-- automático). V6/V7 dejaron el puente y la numeración; esto pone lo que
-- faltaba para que una operación de negocio genere su comprobante sola:
--  1. el FK que faltaba de operacion_contable.tipo_operacion_id
--  2. columnas para la cola de operaciones que no se pudieron contabilizar
--  3. tipo_operacion → tipo_comprobante (qué clase de comprobante emite)
--  4. semillas: 4 tipos de operación con su esquema contable, contra las
--     cuentas afectables que sembró V10

-- ============================================================
-- 1. FK que faltaba: operacion_contable.tipo_operacion_id
--    (V1 lo dejó NOT NULL pero sin referencia; V6 solo agregó el de esquema)
-- ============================================================

ALTER TABLE sc_contabilidad.operacion_contable
    ADD CONSTRAINT fk_operacion_tipo
        FOREIGN KEY (tipo_operacion_id)
        REFERENCES sc_administracion.tipo_operacion (tipo_operacion_id);

CREATE INDEX ix_operacion_tipo
    ON sc_contabilidad.operacion_contable (tipo_operacion_id);

-- Una operación puede quedar pendiente (PND) porque ni su tipo está configurado
-- todavía; en ese caso no hay tipo_operacion_id que poner. Se relaja el NOT NULL.
ALTER TABLE sc_contabilidad.operacion_contable
    ALTER COLUMN tipo_operacion_id DROP NOT NULL;

-- ============================================================
-- 2. Cola de contabilización
--    estado: REG recién creada · PROC contabilizada · PND falló, a reprocesar
-- ============================================================

ALTER TABLE sc_contabilidad.operacion_contable
    ADD COLUMN error_mensaje  VARCHAR(500),
    ADD COLUMN fecha_proceso  TIMESTAMP,
    ADD COLUMN intentos       INTEGER NOT NULL DEFAULT 0;

CREATE INDEX ix_operacion_estado
    ON sc_contabilidad.operacion_contable (estado);

-- ============================================================
-- 3. tipo_operacion → tipo_comprobante
-- ============================================================

ALTER TABLE sc_administracion.tipo_operacion
    ADD COLUMN tipo_comprobante_id BIGINT;

ALTER TABLE sc_administracion.tipo_operacion
    ADD CONSTRAINT fk_tipo_oper_comprobante
        FOREIGN KEY (tipo_comprobante_id)
        REFERENCES sc_contabilidad.tipo_comprobante (tipo_comprobante_id);

-- ============================================================
-- 4. Semillas de configuración
--    Un esquema por operación; su detalle referencia las afectables por código
--    (las creó V10: '1110-BOB' caja, '2110-BOB' cajas de ahorro,
--     '3110-BOB' aportaciones de socios).
-- ============================================================

-- 4.1 esquema_contable (cabecera, versión 1, vigente)
INSERT INTO sc_contabilidad.esquema_contable
    (codigo, nombre, version, estado, descripcion, estacion)
VALUES
    ('DEP_AHO', 'Depósito en caja de ahorro',      1, 'V', 'Debita caja, acredita la obligación con el socio.', 'init'),
    ('RET_AHO', 'Retiro de caja de ahorro',        1, 'V', 'Debita la obligación con el socio, acredita caja.', 'init'),
    ('APO_EMI', 'Emisión de certificado de aporte',1, 'V', 'Debita caja, acredita capital social.',            'init'),
    ('APO_DEV', 'Devolución de aportaciones',       1, 'V', 'Debita capital social, acredita caja.',            'init');

-- 4.2 esquema_contable_detalle (dos renglones por esquema, fórmula = MONTO)
INSERT INTO sc_contabilidad.esquema_contable_detalle
    (esquema_contable_id, secuencia, dom_debe_haber, afectable_id, formula, descripcion)
SELECT e.esquema_contable_id, d.secuencia, d.dh, a.afectable_id, 'MONTO', d.descripcion
FROM (VALUES
    ('DEP_AHO', 1, 'D', '1110-BOB', 'Ingreso de efectivo a caja'),
    ('DEP_AHO', 2, 'H', '2110-BOB', 'Aumento del depósito del socio'),
    ('RET_AHO', 1, 'D', '2110-BOB', 'Disminución del depósito del socio'),
    ('RET_AHO', 2, 'H', '1110-BOB', 'Salida de efectivo de caja'),
    ('APO_EMI', 1, 'D', '1110-BOB', 'Ingreso de efectivo a caja'),
    ('APO_EMI', 2, 'H', '3110-BOB', 'Aumento del capital social'),
    ('APO_DEV', 1, 'D', '3110-BOB', 'Disminución del capital social'),
    ('APO_DEV', 2, 'H', '1110-BOB', 'Salida de efectivo de caja')
) AS d(esquema_codigo, secuencia, dh, afectable_codigo, descripcion)
JOIN sc_contabilidad.esquema_contable e
    ON e.codigo = d.esquema_codigo AND e.version = 1
JOIN sc_contabilidad.cuenta_afectable a
    ON a.codigo = d.afectable_codigo;

-- 4.3 tipo_operacion, ligado a su esquema y al tipo de comprobante que emite
INSERT INTO sc_administracion.tipo_operacion
    (codigo, descripcion, estado, esquema_contable_id, tipo_comprobante_id, estacion)
SELECT t.codigo, t.descripcion, 'V', e.esquema_contable_id, tc.tipo_comprobante_id, 'init'
FROM (VALUES
    ('DEP_AHO', 'Depósito en cuenta de captación',   'ING'),
    ('RET_AHO', 'Retiro de cuenta de captación',     'EGR'),
    ('APO_EMI', 'Emisión de certificado de aporte',  'ING'),
    ('APO_DEV', 'Devolución de aportaciones',        'EGR')
) AS t(codigo, descripcion, comprobante_codigo)
JOIN sc_contabilidad.esquema_contable e
    ON e.codigo = t.codigo AND e.version = 1
JOIN sc_contabilidad.tipo_comprobante tc
    ON tc.codigo = t.comprobante_codigo;
