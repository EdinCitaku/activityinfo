package org.activityinfo.model.expr;

import org.activityinfo.model.expr.diagnostic.ExprSyntaxException;
import org.activityinfo.model.expr.diagnostic.InvalidTypeException;
import org.activityinfo.model.expr.eval.EmptyEvalContext;
import org.activityinfo.model.expr.functions.Casting;
import org.activityinfo.model.type.FieldValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

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

    private void evaluateAndExpectSyntaxException(String exprString) {
        try {
            evaluate(exprString,Double.NaN);
            throw new AssertionError("Input \"" + exprString + "\" expected to cause ExprSyntaxException");
        } catch(ExprSyntaxException excp) { /* Expected Exception */ }
    }

    private void evaluateAndExpectInvalidTypeException(String exprString) {
        try {
            evaluate(exprString,Double.NaN);
            throw new AssertionError("Input \"" + exprString + "\" expected to cause InvalidTypeException");
        } catch (InvalidTypeException excp) { /* Expected Exception */ }
    }

    private void evaluate(String exprString, double expectedValue) {
        ExprLexer lexer = new ExprLexer(exprString);
        ExprParser parser = new ExprParser(lexer);
        ExprNode expr = parser.parse();
        FieldValue value = expr.evaluate(EmptyEvalContext.INSTANCE);
        assertThat(exprString, Casting.toQuantity(value).getValue(), closeTo(expectedValue,0));
    }

    private void evaluate(String exprString, boolean expectedValue) {
        ExprLexer lexer = new ExprLexer(exprString);
        ExprParser parser = new ExprParser(lexer);
        ExprNode expr = parser.parse();
        FieldValue result = expr.evaluate(EmptyEvalContext.INSTANCE);
        assertThat(exprString, Casting.toBoolean(result), equalTo(expectedValue));
    }

}
