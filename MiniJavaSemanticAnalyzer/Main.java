import syntaxtree.*;
import java.io.*;

class Main {
    public static void main (String [] args) throws Exception{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);
            Goal root = parser.Goal();
            System.err.println("Program parsed successfully.");

            SymbolTableBuildVisitor populateSymbolTable = new SymbolTableBuildVisitor();
            root.accept(populateSymbolTable, null);

            SymbolTable symbolTable = SymbolTable.getInstance();
            symbolTable.printOffsets();
//            symbolTable.prettyPrint();
            // TypeCheckVisitor typeCheck = new TypeCheckVisitor(populateSymbolTable.symbolTable);
            // root.accept(populateSymbolTable, null);
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
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