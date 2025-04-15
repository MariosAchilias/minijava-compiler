package SourceToSourceTranslator.IRToJava;

import SourceToSourceTranslator.SourceToIR.Lexer;
import SourceToSourceTranslator.SourceToIR.Parser;

import java.io.InputStreamReader;

class Main {
    public static void main(String[] argv) throws Exception{
        Parser p = new Parser(new Lexer(new InputStreamReader(System.in)));
        p.parse();
    }
}
