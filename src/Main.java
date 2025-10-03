import java.io.*;
import syntaxtree.*;
import SymbolTable.SymbolTable;

class Main {
    public static void main (String [] args) throws Exception{
        FileInputStream fis = null;
        for (String fileName : args) {
            try {
                fis = new FileInputStream(fileName);
                MiniJavaParser parser = new MiniJavaParser(fis);
                Goal root = parser.Goal();
                System.out.println("Running semantic check on " + fileName);

                SymbolTable symbolTable = new SymbolTable();
                SymbolTableBuildVisitor populateSymbolTable = new SymbolTableBuildVisitor(symbolTable);
                root.accept(populateSymbolTable, null);

                symbolTable.printOffsets();
                TypeCheckVisitor typeCheck = new TypeCheckVisitor(symbolTable);
                root.accept(typeCheck, null);

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