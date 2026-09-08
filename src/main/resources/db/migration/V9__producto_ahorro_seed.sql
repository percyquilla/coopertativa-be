-- Producto de ahorro semilla (CONTEXTO-BD.md §7 Paso 6). V1 solo dejó el
-- producto de aportación 'CA'; para abrir cuentas de captación de ahorro hace
-- falta al menos un producto tipo 'AH'.

INSERT INTO sc_administracion.producto
    (codigo, nombre, descripcion, tipo_producto, moneda, saldo_minimo,
     permite_deposito, permite_retiro, permite_interes, estacion)
VALUES
    ('AH01', 'Caja de ahorro', 'Cuenta de ahorro a la vista en bolivianos.',
     'AH', 'BOB', 50, TRUE, TRUE, TRUE, 'init');
