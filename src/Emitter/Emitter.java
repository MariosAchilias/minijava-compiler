package Emitter;

import java.io.*;
import java.util.LinkedHashMap;

import SymbolTable.*;
import SymbolTable.Class;

public class Emitter {
    FileOutputStream outFile;
    int registerCount;
    LinkedHashMap<String, String> variableToRegister;
    public Emitter(FileOutputStream outFile) {
        this.outFile = outFile;
        registerCount = 0;
    }

    public String newRegister () {
        return "%_" + registerCount++;
    }

    public void declareVar(String type, String name) {
        // TODO: create register, insert into variableToRegister and emit declaration
    }

    private int buildMethodDecls(Class c, StringBuilder methodDecls) {
        if (c == null)
            return 0;

        int cnt = buildMethodDecls(c.getParent(), methodDecls);

        for (Method m : c.getMethods()) {
            boolean isOverride = c.getParent() == null
                                ? false
                                : (c.getLocalMethod(m.id) != null) && (c.getParent().getMethod(m.id) != null);
            if (isOverride)
                continue;
            
            cnt += 1;

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

    public void emitVTables(SymbolTable st) throws IOException {
        outFile.write(String.format("@.%s_vtable = global [0 x i8*] []\n", st.main.name).getBytes());
        for (Class c : st.getClasses()) {
            StringBuilder methodDecls = new StringBuilder();
            int methodCount = buildMethodDecls(c, methodDecls);
            outFile.write(String.format("@.%s_vtable = global [%d x i8*] [%s]\n", c.name, methodCount, methodDecls.toString()).getBytes());
        }
    }

    public void emitHelpers() throws IOException {
        outFile.write(("declare i8* @calloc(i32, i32)\n" + //
                        "declare i32 @printf(i8*, ...)\n" + //
                        "declare void @exit(i32)\n" + //
                        "\n" + //
                        "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" + //
                        "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" + //
                        "define void @print_int(i32 %i) {\n" + //
                        "    %_str = bitcast [4 x i8]* @_cint to i8*\n" + //
                        "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" + //
                        "    ret void\n" + //
                        "}\n" + //
                        "\n" + //
                        "define void @throw_oob() {\n" + //
                        "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" + //
                        "    call i32 (i8*, ...) @printf(i8* %_str)\n" + //
                        "    call void @exit(i32 1)\n" + //
                        "    ret void\n" + //
                        "}").getBytes());
    };

    private static String typeToLLVM(String type) {
        switch (type) {
            case "int":       return "i32";
            case "boolean":   return "i1"; 
            default:          return "i8*";
        }
    }
}
