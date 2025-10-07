import Emitter.*;
import Exceptions.ParseException;
import Exceptions.SemanticException;
import Parser.*;
import Parser.syntaxtree.*;
import SymbolTable.SymbolTable;
import java.io.*;

class Main {
    public static void main (String [] args) throws Exception{
        FileInputStream fis = null;
        FileOutputStream fout = null;
        for (String fileName : args) {
            try {
                fis = new FileInputStream(fileName);
                fout = new FileOutputStream(fileName.replace(".java", ".ll"));
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();

                SymbolTable symbolTable = new SymbolTable();
                SymbolTableBuildVisitor populateSymbolTable = new SymbolTableBuildVisitor(symbolTable);
                root.accept(populateSymbolTable, null);

                TypeCheckVisitor typeCheck = new TypeCheckVisitor(symbolTable);
                root.accept(typeCheck, null);

                Emitter emitter = new Emitter(fout, symbolTable);
                EmitIRVisitor emitIR = new EmitIRVisitor(symbolTable, emitter);
                root.accept(emitIR, null);

            }
            catch(ParseException ex){
                System.err.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(SemanticException ex) {
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}