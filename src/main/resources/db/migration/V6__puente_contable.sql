-- Puente operación → contabilidad (CONTEXTO-BD.md §5.3 / §7 Paso 7).
--
-- Cierra tres huecos:
--  1. operacion_contable no sabía qué transacción de negocio la generó ni
--     qué esquema contable se le aplicó (RF-CON-006, asiento automático).
--  2. tipo_operacion no estaba ligado al esquema_contable que le toca.
--  3. No había forma de referenciar el comprobante/operación original en
--     una reversión.
-- Además refuerza el trigger de partida doble de V1.
--
-- Sin Java: el motor contabilizador que interpreta esquema_contable_detalle
-- y genera el comprobante es trabajo aparte (§6, "el corazón del sistema").

-- ============================================================
-- 1. Trazabilidad y esquema en operacion_contable
-- ============================================================

ALTER TABLE sc_contabilidad.operacion_contable
    ADD COLUMN esquema_contable_id     BIGINT,
    ADD COLUMN origen_modulo           VARCHAR(30),
    ADD COLUMN origen_tabla            VARCHAR(60),
    ADD COLUMN origen_id               BIGINT,
    ADD COLUMN operacion_reversion_id  BIGINT;

ALTER TABLE sc_contabilidad.operacion_contable
    ADD CONSTRAINT fk_operacion_esquema
        FOREIGN KEY (esquema_contable_id)
        REFERENCES sc_contabilidad.esquema_contable (esquema_contable_id),
    ADD CONSTRAINT fk_operacion_reversion
        FOREIGN KEY (operacion_reversion_id)
        REFERENCES sc_contabilidad.operacion_contable (operacion_id);

CREATE INDEX ix_operacion_esquema
    ON sc_contabilidad.operacion_contable (esquema_contable_id);
CREATE INDEX ix_operacion_reversion
    ON sc_contabilidad.operacion_contable (operacion_reversion_id);
CREATE INDEX ix_operacion_origen
    ON sc_contabilidad.operacion_contable (origen_modulo, origen_tabla, origen_id);

-- ============================================================
-- 2. tipo_operacion → esquema_contable
-- (FK entre esquemas: sc_administracion → sc_contabilidad)
-- ============================================================

ALTER TABLE sc_administracion.tipo_operacion
    ADD COLUMN esquema_contable_id BIGINT;

ALTER TABLE sc_administracion.tipo_operacion
    ADD CONSTRAINT fk_tipo_oper_esquema
        FOREIGN KEY (esquema_contable_id)
        REFERENCES sc_contabilidad.esquema_contable (esquema_contable_id);

CREATE INDEX ix_tipo_oper_esquema
    ON sc_administracion.tipo_operacion (esquema_contable_id);

-- ============================================================
-- 3. Reversión de comprobante con referencia al original
-- ============================================================

ALTER TABLE sc_contabilidad.comprobante
    ADD COLUMN comprobante_reversion_id BIGINT;

ALTER TABLE sc_contabilidad.comprobante
    ADD CONSTRAINT fk_comprobante_reversion
        FOREIGN KEY (comprobante_reversion_id)
        REFERENCES sc_contabilidad.comprobante (comprobante_id);

CREATE INDEX ix_comprobante_reversion
    ON sc_contabilidad.comprobante (comprobante_reversion_id);

-- ============================================================
-- 4. Trigger de partida doble reforzado (§4.9)
-- Rechaza autorizar un comprobante sin renglones o con importe cero,
-- además de exigir SUM(debe) = SUM(haber).
-- ============================================================

CREATE OR REPLACE FUNCTION sc_contabilidad.fn_validar_partida_doble()
RETURNS TRIGGER AS $$
DECLARE
    v_renglones INTEGER;
    v_debe      NUMERIC(20,6);
    v_haber     NUMERIC(20,6);
BEGIN
    SELECT
        COUNT(*),
        COALESCE(SUM(monto) FILTER (WHERE dom_debe_haber = 'D'), 0),
        COALESCE(SUM(monto) FILTER (WHERE dom_debe_haber = 'H'), 0)
    INTO v_renglones, v_debe, v_haber
    FROM sc_contabilidad.comprobante_reng
    WHERE comprobante_id = NEW.comprobante_id;

    IF v_renglones = 0 THEN
        RAISE EXCEPTION 'Comprobante % no tiene renglones', NEW.comprobante_id;
    END IF;

    IF v_debe <> v_haber THEN
        RAISE EXCEPTION 'Comprobante % no cuadra: debe=% haber=%', NEW.comprobante_id, v_debe, v_haber;
    END IF;

    IF v_debe = 0 THEN
        RAISE EXCEPTION 'Comprobante % tiene importe cero', NEW.comprobante_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
