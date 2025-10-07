package Emitter;

import SymbolTable.*;
import SymbolTable.Class;
import java.io.*;
import java.util.LinkedHashMap;

final class Vtable extends LinkedHashMap<String, Integer> {}; // method name -> offset

public class Emitter {
    FileOutputStream outFile;
    int registerCount = 0;
    int indentation = 0;
    LinkedHashMap<String, String> variableToRegister = null;
    LinkedHashMap<String, Vtable> classToVtable =  new LinkedHashMap<>();
    public Emitter(FileOutputStream outFile) {
        this.outFile = outFile;
    }

    public String newRegister () {
        return "%_" + registerCount++;
    }

    public void declareVar(String type, String name) {
        // TODO: create register, insert into variableToRegister and emit declaration
    }

    private void emitLine(String line) throws IOException {
        String s = "\t".repeat(indentation) + line + "\n";
        outFile.write(s.getBytes());
    }

    public void methodDefinitionStart(Method m) throws IOException {
        var args = new StringBuilder();
        for (Variable a : m.parameters) {
            args.append(String.format(", %s %%%s", typeToLLVM(a.varType), a.id));
        }
        
        // All methods have a 'this' pointer as their first argument
        String def = String.format("\ndefine %s @%s(i8* %%this %s) {\n", typeToLLVM(m.returnType), m.id, args.toString());
        emitLine(def);
        indentation++;
        variableToRegister = new LinkedHashMap<>();

        // Allocate parameters
        for (Variable a : m.parameters) {
            String type = typeToLLVM(a.varType);
            String reg = newRegister();
            variableToRegister.put(a.id, reg);
            emitLine(String.format("%s = alloca %s", reg, type));
            emitLine(String.format("store %s %s, %s* %s\n", type, "%" + a.id, type, reg));
        }

    }

    public void methodDefinitionEnd() throws IOException {
        outFile.write("}\n".getBytes());
        variableToRegister = null;
        indentation--;
    }

    public void emitVTables(SymbolTable st) throws IOException {
        outFile.write(String.format("@.%s_vtable = global [0 x i8*] []\n", st.main.name).getBytes());
        for (Class c : st.getClasses()) {
            StringBuilder methodDecls = new StringBuilder();
            var vt = new Vtable();
            int methodCount = buildMethodDecls(c, methodDecls, vt);
            classToVtable.put(c.name, vt);
            outFile.write(String.format("@.%s_vtable = global [%d x i8*] [%s]\n", c.name, methodCount, methodDecls.toString()).getBytes());
        }
    }

    public void emitHelpers() throws IOException {
        outFile.write(("""
                       declare i8* @calloc(i32, i32)
                       declare i32 @printf(i8*, ...)
                       declare void @exit(i32)
                       
                       @_cint = constant [4 x i8] c"%d\\0a\\00"
                       @_cOOB = constant [15 x i8] c"Out of bounds\\0a\\00"
                       define void @print_int(i32 %i) {
                           %_str = bitcast [4 x i8]* @_cint to i8*
                           call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
                           ret void
                       }
                       
                       define void @throw_oob() {
                           %_str = bitcast [15 x i8]* @_cOOB to i8*
                           call i32 (i8*, ...) @printf(i8* %_str)
                           call void @exit(i32 1)
                           ret void
                       }""").getBytes());
    };

    private static String typeToLLVM(String type) {
        // TODO: arrays
        return switch (type) {
            case "int" -> "i32";
            case "boolean" -> "i1";
            default -> "i8*";
        };
    }

    private int buildMethodDecls(Class c, StringBuilder methodDecls, Vtable vtable) {
        if (c == null)
            return 0;

        int cnt = buildMethodDecls(c.getParent(), methodDecls, vtable);

        for (Method m : c.getMethods()) {
            boolean isOverride = c.getParent() == null
                                ? false
                                : (c.getLocalMethod(m.id) != null) && (c.getParent().getMethod(m.id) != null);
            if (isOverride)
                continue;
            
            vtable.put(m.id, cnt++);

            var args = new StringBuilder();
            for (Variable a : m.parameters)
                args.append(String.format(", %s", typeToLLVM(a.varType)));

            // All methods have a 'this' pointer as their first argument
            String signature = String.format("%s (i8*%s)", typeToLLVM(m.returnType), args.toString()); 
            String decl = String.format("i8* bitcast (%s* @%s to i8*)", signature, m.id);
            if (methodDecls.length() > 0) methodDecls.append(", ");
            methodDecls.append(decl);
        }
        
        return cnt;
    }
}
