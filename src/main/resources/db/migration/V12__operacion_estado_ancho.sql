-- operacion_contable.estado nació como VARCHAR(3) (REG). El motor la deja en
-- 'PROC' (contabilizada) o 'PND' (pendiente); 'PROC' no entra en 3 caracteres.
-- Se ensancha a VARCHAR(15), como el resto de las columnas de estado del modelo.

ALTER TABLE sc_contabilidad.operacion_contable
    ALTER COLUMN estado TYPE VARCHAR(15);
