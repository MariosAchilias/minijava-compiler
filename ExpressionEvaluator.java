import java.io.IOException;

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

    private int Expr() throws IOException, ParseError {
        return 0;
    }
}
