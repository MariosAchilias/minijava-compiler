package Emitter;

import java.io.*;
import SymbolTable.*;
import SymbolTable.Class;

public class Emitter {
    FileOutputStream outFile;
    int registerCount;
    public Emitter(FileOutputStream outFile) {
        this.outFile = outFile;
        registerCount = 0;
    }

    public String newRegister () {
        return "%_" + registerCount++;
    }

    private int buildMethodDecls(Class c, StringBuilder methodDecls) {
        if (c == null)
            return 0;

    

        int ret = buildMethodDecls(c.getParent(), methodDecls);

        // TODO: use a vtable object instead
        // for (Method m : c.getMethods()) {
        //     boolean isOverride = (c.getLocalMethod(m.id) != null) && (c.getParent().getMethod(m.id) != null);
        //     if (isOverride)
        //         continue;
            
        //     ret += 1;

        // }
        
        // methodDecls.append(String.format("allmethodsof(%s)", c.name));
        return ret;
    }

    private void emitVTable(Class c) throws IOException {
        StringBuilder methodDecls = new StringBuilder();
        int methodCount = buildMethodDecls(c, methodDecls);
        outFile.write(String.format("@.%s_vtable = global [%d x i8*] [%s]\n", c.name, methodCount, methodDecls.toString()).getBytes());
    }

    public void emitVTables(SymbolTable st) throws IOException {
        outFile.write(String.format("@.%s_vtable = global [0 x i8*] []\n", st.main.name).getBytes());
        for (Class c : st.getClasses())
            emitVTable(c);
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
}
