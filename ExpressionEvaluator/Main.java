package ExpressionEvaluator;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println((new ExpressionEvaluator(System.in)).eval());
        } catch (IOException | ParseError e) {
            System.err.println(e.getMessage());
        }
    }
}
