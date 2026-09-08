-- Semilla mínima de monedas (CONTEXTO-BD.md §7 Paso 4). El diseño base creó
-- sc_administracion.moneda en V1 pero sin filas; tipo_cambio (V3) y toda la
-- contabilidad la referencian. Se cargan las dos monedas operativas y se fija
-- la moneda base de la institución.

INSERT INTO sc_administracion.moneda (codigo, desripcion, simbolo, estado) VALUES
    ('BOB', 'Boliviano',              'Bs',    'V'),
    ('USD', 'Dólar estadounidense',   'USD $', 'V');

UPDATE sc_parametros.institucion
   SET moneda_base_id = (SELECT moneda_id FROM sc_administracion.moneda WHERE codigo = 'BOB')
 WHERE codigo = 'PRINCIPAL' AND moneda_base_id IS NULL;
