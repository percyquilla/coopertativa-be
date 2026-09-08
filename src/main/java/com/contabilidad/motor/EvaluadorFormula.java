package com.contabilidad.motor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Evalúa las fórmulas de {@code esquema_contable_detalle.formula}: aritmética
 * sobre variables del contexto de la operación.
 * <p>
 * Gramática, por descenso recursivo:
 * <pre>
 *   expr   = term  (('+' | '-') term)*
 *   term   = factor (('*' | '/') factor)*
 *   factor = '-' factor | '(' expr ')' | numero | VARIABLE
 * </pre>
 * Todo en {@link BigDecimal}; la división usa escala 6 y {@code HALF_UP}. No hay
 * funciones, ni llamadas, ni acceso a nada fuera del mapa de variables — una
 * fórmula no puede ejecutar código, solo calcular un número.
 */
public final class EvaluadorFormula {

    private static final int ESCALA_DIVISION = 6;

    private final String formula;
    private final Map<String, BigDecimal> variables;
    private final String contexto;
    private final boolean soloSintaxis;
    private int pos;

    private EvaluadorFormula(String formula, Map<String, BigDecimal> variables, String contexto,
                             boolean soloSintaxis) {
        this.formula = formula;
        this.variables = variables;
        this.contexto = contexto;
        this.soloSintaxis = soloSintaxis;
    }

    /**
     * Verifica que la fórmula esté bien escrita, sin conocer las variables (toda
     * variable vale 1). Se usa al guardar un esquema, donde todavía no se sabe
     * qué operación lo va a usar.
     */
    public static void validarSintaxis(String formula, String contexto) {
        if (formula == null || formula.isBlank()) {
            throw new FormulaInvalidaException(contexto + ": la fórmula está vacía.");
        }
        EvaluadorFormula e = new EvaluadorFormula(formula, Map.of(), contexto, true);
        e.expr();
        e.saltarEspacios();
        if (e.pos < formula.length()) {
            throw new FormulaInvalidaException(contexto + ": sobra «"
                    + formula.substring(e.pos).trim() + "» al final de la fórmula.");
        }
    }

    /**
     * @param contexto texto para los mensajes de error ("la operación DEP_AHO,
     *                 renglón 3"); no afecta el cálculo.
     */
    public static BigDecimal evaluar(String formula, Map<String, BigDecimal> variables, String contexto) {
        if (formula == null || formula.isBlank()) {
            throw new FormulaInvalidaException(contexto + ": la fórmula está vacía.");
        }
        EvaluadorFormula e = new EvaluadorFormula(formula, variables, contexto, false);
        BigDecimal valor = e.expr();
        e.saltarEspacios();
        if (e.pos < formula.length()) {
            throw new FormulaInvalidaException(contexto + ": sobra «"
                    + formula.substring(e.pos).trim() + "» al final de la fórmula.");
        }
        return valor;
    }

    // ---- gramática ----

    private BigDecimal expr() {
        BigDecimal valor = term();
        while (true) {
            char op = siguienteOperador("+-");
            if (op == 0) {
                return valor;
            }
            pos++;
            BigDecimal derecha = term();
            valor = op == '+' ? valor.add(derecha) : valor.subtract(derecha);
        }
    }

    private BigDecimal term() {
        BigDecimal valor = factor();
        while (true) {
            char op = siguienteOperador("*/");
            if (op == 0) {
                return valor;
            }
            pos++;
            BigDecimal derecha = factor();
            if (op == '*') {
                valor = valor.multiply(derecha);
            } else {
                if (derecha.signum() == 0) {
                    throw new FormulaInvalidaException(contexto + ": la fórmula divide por cero.");
                }
                valor = valor.divide(derecha, ESCALA_DIVISION, RoundingMode.HALF_UP);
            }
        }
    }

    private BigDecimal factor() {
        saltarEspacios();
        if (pos >= formula.length()) {
            throw new FormulaInvalidaException(contexto + ": la fórmula termina donde se esperaba un valor.");
        }
        char c = formula.charAt(pos);

        if (c == '-') {
            pos++;
            return factor().negate();
        }
        if (c == '+') {
            pos++;
            return factor();
        }
        if (c == '(') {
            pos++;
            BigDecimal valor = expr();
            saltarEspacios();
            if (pos >= formula.length() || formula.charAt(pos) != ')') {
                throw new FormulaInvalidaException(contexto + ": falta cerrar un paréntesis.");
            }
            pos++;
            return valor;
        }
        if (Character.isDigit(c) || c == '.') {
            return numero();
        }
        if (Character.isLetter(c) || c == '_') {
            return variable();
        }
        throw new FormulaInvalidaException(contexto + ": no se entiende «" + c + "» en la fórmula.");
    }

    private BigDecimal numero() {
        int inicio = pos;
        while (pos < formula.length()
                && (Character.isDigit(formula.charAt(pos)) || formula.charAt(pos) == '.')) {
            pos++;
        }
        try {
            return new BigDecimal(formula.substring(inicio, pos));
        } catch (NumberFormatException e) {
            throw new FormulaInvalidaException(contexto + ": «"
                    + formula.substring(inicio, pos) + "» no es un número válido.");
        }
    }

    private BigDecimal variable() {
        int inicio = pos;
        while (pos < formula.length()
                && (Character.isLetterOrDigit(formula.charAt(pos)) || formula.charAt(pos) == '_')) {
            pos++;
        }
        String nombre = formula.substring(inicio, pos);
        if (soloSintaxis) {
            return BigDecimal.ONE;
        }
        BigDecimal valor = variables.get(nombre);
        if (valor == null) {
            throw new FormulaInvalidaException(contexto + ": la fórmula usa «" + nombre
                    + "», que la operación no provee. Variables disponibles: "
                    + (variables.isEmpty() ? "(ninguna)" : String.join(", ", variables.keySet())) + ".");
        }
        return valor;
    }

    // ---- utilitarios ----

    private char siguienteOperador(String operadores) {
        saltarEspacios();
        if (pos < formula.length() && operadores.indexOf(formula.charAt(pos)) >= 0) {
            return formula.charAt(pos);
        }
        return 0;
    }

    private void saltarEspacios() {
        while (pos < formula.length() && Character.isWhitespace(formula.charAt(pos))) {
            pos++;
        }
    }

    /** Fórmula mal escrita o con una variable que el contexto no tiene. */
    public static class FormulaInvalidaException extends RuntimeException {
        public FormulaInvalidaException(String mensaje) {
            super(mensaje);
        }
    }
}
