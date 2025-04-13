package ExpressionEvaluator;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionEvaluator {
    private final InputStream in;
    private int lookahead;

    public ExpressionEvaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    public int eval() throws IOException, ParseError {
        int value = Expr();
        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();
        return value;
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    private int Expr() throws IOException, ParseError {
        int value = PowExpr();
        return ExprTail(value);
    }

    private int ExprTail(int leftop) throws IOException, ParseError {
        int rightop;
        switch (lookahead) {
            case '+':
                consume('+');
                rightop = PowExpr();
                return ExprTail(leftop + rightop);
            case '-':
                consume('-');
                rightop = PowExpr();
                return ExprTail(leftop - rightop);
            default:
                return leftop;
        }
    }

    private int PowExpr() throws IOException, ParseError {
        int val = Factor();
        return PowExprTail(val);
    }

    private int PowExprTail(int left) throws IOException, ParseError {
        if (lookahead == '*') {
            consume('*');
            consume('*');
            int val = Factor();
            return PowExprTail((int) Math.pow(left, val));
        }

        return left;
    }

    private int Factor() throws IOException, ParseError {
        if (lookahead == '(') {
            consume('(');
            int val = Expr();
            consume(')');
            return val;
        }
        if (isDigit(lookahead)) {
            int value = evalDigit(lookahead);
            consume(lookahead);
            return value;
        }
        throw new ParseError();
    }

}
