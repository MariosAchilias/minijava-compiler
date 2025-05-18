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
            SemanticAnalysisVisitor eval = new SemanticAnalysisVisitor();
            root.accept(eval, null);
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