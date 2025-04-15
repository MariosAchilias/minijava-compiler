package SourceToSourceTranslator.SourceToIR;

import java_cup.runtime.*;
import java.io.*;

import SourceToSourceTranslator.SourceToIR.Lexer;
import SourceToSourceTranslator.SourceToIR.Parser;

class Main {
    public static void main(String[] argv) throws Exception{
        Parser p = new Parser(new Lexer(new InputStreamReader(System.in)));
        p.parse();
    }
}
