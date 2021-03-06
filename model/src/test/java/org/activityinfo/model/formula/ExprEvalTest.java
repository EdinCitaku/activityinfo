/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.formula;

import org.activityinfo.model.formula.diagnostic.FormulaSyntaxException;
import org.activityinfo.model.formula.diagnostic.InvalidTypeException;
import org.activityinfo.model.formula.eval.EmptyEvalContext;
import org.activityinfo.model.formula.functions.Casting;
import org.activityinfo.model.type.FieldValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ExprEvalTest {

    @Test
    public void evaluateExpr() {
        evaluate("1", 1);
        evaluate("1+1", 2);
        evaluate("(5+5)/2", 5);
    }

    @Test
    public void evaluateUnaryOperators() {
        // PlusFunction Unary
        evaluate("+1",1);
        evaluate("+1.0",1.0);
        evaluate("+1*2",2);
        evaluate("+1.0*2.0",2.0);
        evaluate("2*(+1)",2);
        evaluate("2.0*(+1.0)",2.0);

        // MinusFunction Unary
        evaluate("-1",-1);
        evaluate("-1.0",-1.0);
        evaluate("-1*2",-2);
        evaluate("-1.0*2.0",-2.0);
        evaluate("2*(-1)",-2);
        evaluate("2.0*(-1.0)",-2.0);

        // MultiplyFunction Unary - should throw exception
        evaluateAndExpectSyntaxException("*1");
        evaluateAndExpectSyntaxException("*1.0");
        evaluateAndExpectSyntaxException("1*");
        evaluateAndExpectSyntaxException("1.0*");

        // DivideFunction Unary - should throw exception
        evaluateAndExpectSyntaxException("/1");
        evaluateAndExpectSyntaxException("/1.0");
        evaluateAndExpectSyntaxException("1/");
        evaluateAndExpectSyntaxException("1.0/");
    }

    @Test
    public void evaluateRoundingExpr() {
        evaluate("CEIL(1.5)",2.0);
        evaluate("CEIL(-1.5)",-1.0);
        evaluate("FLOOR(1.5)",1.0);
        evaluate("FLOOR(-1.5)",-2.0);

        // Invalid types - should throw exception
        evaluateAndExpectInvalidTypeException("CEIL(\"test\")");
        evaluateAndExpectInvalidTypeException("FLOOR(\"test\")");
        evaluateAndExpectInvalidTypeException("CEIL(TRUE)");
        evaluateAndExpectInvalidTypeException("FLOOR(TRUE)");
    }

    @Test
    public void evaluateBooleanExpr() {
        evaluate("true", true);
        evaluate("false", false);
        evaluate("true&&true", true);
        evaluate("true&&false", false);
        evaluate("true||false", true);
        evaluate("false||false", false);
        evaluate("false||false||true", true);
        evaluate("(false||true)&&true", true);
        evaluate("true==true", true);
        evaluate("true==false", false);
        evaluate("true!=false", true);
        evaluate("false!=false", false);
        evaluate("true!=true", false);
        evaluate("!true", false);
        evaluate("!false", true);

        evaluate("2>1", true);
        evaluate("1>2", false);
        evaluate("4>=3", true);
        evaluate("3>=3", true);
        evaluate("1>=3", false);
        evaluate("1<3", true);
        evaluate("1<0", false);
        evaluate("1<=4", true);
        evaluate("1<=1", true);
        evaluate("1<=0", false);


        evaluate(" (3 < 4) && (4 < 5)", true);
        evaluate(" (3 <= 3) && (4 <= 5)", true);
        evaluate(" (3 > 2) && (4 > 5)", false);
    }

    @Test
    public void searchTest() {
        evaluate("SEARCH('apple', 'An apple a day')", 4);
        evaluate("SEARCH('APPLE', 'An apple a day')", 4);
        evaluate("SEARCH('An', 'An apple a day')", 1);

        evaluate("SEARCH('Foo', 'Foobar FOOBAR Foobar')", 1);
        evaluate("SEARCH('Foo', 'Foobar FOOBAR Foobar', 2)", 8);
        evaluate("SEARCH('Foo', 'Foobar FOOBAR Foobar', 9)", 15);
        evaluate("SEARCH('Foo', 'Foobar FOOBAR Foobar', 20)", Double.NaN);
    }

    @Test
    public void isNumber() {

        evaluate("ISNUMBER(4)", true);
        evaluate("ISNUMBER(1/0)", false);
        evaluate("ISNUMBER('foobar')", false);
        evaluate("ISNUMBER(SEARCH('needle', 'haystack'))", false);
    }

    @Test
    public void dateLiterals() {
        evaluate("YEARFRAC(DATE(2017, 1, 1), DATE(2019, 1, 1))", 2.0);
    }

    @Test
    public void dateComparison() {
        evaluate("DATE(2017,1,1) < DATE(2017,1,30)", true);
        evaluate("DATE(2017,1,1) >= DATE(2017,1,30)", false);
        evaluate("DATE(2017,1,1) == DATE(2017,1,30)", false);
        evaluate("DATE(2017,1,1) == DATE(2017,1,1)", true);
    }

    private void evaluateAndExpectSyntaxException(String exprString) {
        try {
            evaluate(exprString,Double.NaN);
            throw new AssertionError("Input \"" + exprString + "\" expected to cause ExprSyntaxException");
        } catch(FormulaSyntaxException excp) { /* Expected Exception */ }
    }

    private void evaluateAndExpectInvalidTypeException(String exprString) {
        try {
            evaluate(exprString,Double.NaN);
            throw new AssertionError("Input \"" + exprString + "\" expected to cause InvalidTypeException");
        } catch (InvalidTypeException excp) { /* Expected Exception */ }
    }

    private void evaluate(String exprString, double expectedValue) {
        FormulaLexer lexer = new FormulaLexer(exprString);
        FormulaParser parser = new FormulaParser(lexer);
        FormulaNode expr = parser.parse();
        FieldValue value = expr.evaluate(EmptyEvalContext.INSTANCE);
        if(Double.isNaN(expectedValue)) {
            assertTrue(exprString + " is NaN", Double.isNaN(Casting.toQuantity(value).getValue()));
        } else {
            assertThat(exprString, Casting.toQuantity(value).getValue(), closeTo(expectedValue, 0));
        }

    }

    private void evaluate(String exprString, boolean expectedValue) {
        FormulaLexer lexer = new FormulaLexer(exprString);
        FormulaParser parser = new FormulaParser(lexer);
        FormulaNode expr = parser.parse();
        FieldValue result = expr.evaluate(EmptyEvalContext.INSTANCE);
        assertThat(exprString, Casting.toBoolean(result), equalTo(expectedValue));
    }

}
