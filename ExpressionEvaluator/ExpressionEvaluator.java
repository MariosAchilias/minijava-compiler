package ExpressionEvaluator;

import java.io.IOException;
import java.io.InputStream;

public class ExpressionEvaluator {
    private final InputStream in;
    private int lookahead;

    public ExpressionEvaluator(InputStream in) throws IOException {
        this.in = in;
        do lookahead = in.read();
        while (isWhitespace(lookahead));
    }

    public int eval() throws IOException, ParseError {
        int value = Expr();
        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();
        return value;
    }

    private void consume(int symbol, boolean ignoreWhitespace) throws IOException, ParseError {
        if (lookahead != symbol)
            throw new ParseError();

        lookahead = in.read();
        if (!ignoreWhitespace)
            return;
        while (isWhitespace(lookahead)) {
            lookahead = in.read();
        }
    }

    private boolean isDigit(int c) { return '0' <= c && c <= '9'; }

    private boolean isWhitespace(int c) { return c == ' ' || c == '\t'; }

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
                consume('+', true);
                rightop = PowExpr();
                return ExprTail(leftop + rightop);
            case '-':
                consume('-', true);
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
            consume('*', false);
            consume('*', true);
            int val = Factor();
            return PowExprTail((int) Math.pow(left, val));
        }

        return left;
    }

    private int Factor() throws IOException, ParseError {
        if (lookahead == '(') {
            consume('(', true);
            int val = Expr();
            consume(')', true);
            return val;
        }

        return Num();
    }

    private int Num() throws IOException, ParseError {
        if (!isDigit(lookahead))
            throw new ParseError();

        String first = Integer.toString(evalDigit(lookahead));
        consume(lookahead, false);
        return Integer.parseInt(NumTail(first));
    }

    private String NumTail(String accum) throws IOException, ParseError {
        if (!isDigit(lookahead)) {
            // skip trailing whitespace
            if (isWhitespace(lookahead))
                consume(lookahead, true);
            return accum;
        }
        String res = accum.concat(Integer.toString(evalDigit(lookahead)));
        consume(lookahead, false);
        return NumTail(res);
    }
}
