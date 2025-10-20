package Emitter;

import SymbolTable.*;
import SymbolTable.Class;
import java.io.*;
import java.util.LinkedHashMap;

public class Emitter {
    FileOutputStream outFile;
    int registerCount = 0;
    int indentation = 0;
    LinkedHashMap<String, String> variableToRegister = null;
    LinkedHashMap<String, VTable> classToVtable =  new LinkedHashMap<>();
    public Emitter(FileOutputStream outFile) {
        this.outFile = outFile;
    }

    public String newRegister () {
        return "%_" + registerCount++;
    }

    private void emitLine(String line) throws IOException {
        String s = "\t".repeat(indentation) + line + "\n";
        outFile.write(s.getBytes());
    }

    public void emitMethodStart(Class c, Method m) throws IOException {
        var args = new StringBuilder();
        for (Variable a : m.parameters) {
            args.append(String.format(", %s %%%s", typeToLLVM(a.varType), a.id));
        }

        if (m.id.equals("main")) {
            emitLine(String.format("\ndefine void @main() {\n")); // TODO main args
        } else {
            // All methods have a 'this' pointer as their first argument
            emitLine(String.format("\ndefine %s @%s_%s(i8* %%this %s) {\n", typeToLLVM(m.returnType), c.name, m.id, args.toString()));
        }
        indentation++;
        variableToRegister = new LinkedHashMap<>();

        // Allocate parameters & local variables
        for (Variable a : m.getLocalScope().getValues()) {
            String type = typeToLLVM(a.varType);
            String reg = newRegister();
            variableToRegister.put(a.id, reg);
            emitLine(String.format("%s = alloca %s", reg, type));
            // For parameters, initialize to given values
            if (m.parameters.contains(a))
                emitLine(String.format("store %s %s, %s* %s\n", type, "%" + a.id, type, reg));
        }

    }

    public void emitMethodEnd(Method m, String ret) throws IOException {
        emitLine(String.format("ret %s %s", typeToLLVM(m.returnType), ret));
        outFile.write("}\n".getBytes());
        variableToRegister = null;
        indentation--;
    }

    public String emitCall(String objectReg, Class c, Method m, java.util.ArrayList<String> args) throws Exception {
        String ret = newRegister();
        int vtOffset = classToVtable.get(c.name).get(m.id);

        // object layout in memory: {i8** vtable_ptr : 8 bytes, fields}
        String tmp = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to i8***", tmp, objectReg));
        String tmp_ = newRegister();
        emitLine(String.format("%s = load i8**, i8*** %s", tmp_, tmp));
        tmp = newRegister();
        emitLine(String.format("%s = getelementptr i8*, i8** %s, i32 %d", tmp, tmp_, vtOffset));
        tmp_ = newRegister();
        emitLine(String.format("%s = load i8*, i8** %s", tmp_, tmp));

        StringBuilder argTypes = new StringBuilder("i8*");
        for (Variable p: m.parameters) {
            argTypes.append(String.format(", %s", typeToLLVM(p.varType)));
        }

        String retType = typeToLLVM(m.returnType);
        String funcPtr = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to %s(%s)*", funcPtr, tmp_, retType, argTypes));

        StringBuilder argRegs = new StringBuilder(String.format("i8* %s", objectReg)); // 'this'
        for (String a: args) {
            // a contains comma separated type and register
            String type = typeToLLVM(a.split(",")[0]);
            String reg = a.split(",")[1];
            System.out.println(String.format("type of arg: %s", type));
            argRegs.append(String.format(", %s %s", type, reg));
        }

        emitLine(String.format("%s = call %s %s(%s)", ret, retType, funcPtr, argRegs));

        return ret;
    }

    public String emitBinaryOperation(String operator, String leftOperand, String rightOperand) throws IOException {
        String reg = newRegister();
        if (operator.equals("slt")) {
            emitLine(String.format("%s = icmp slt i32 %s, %s", reg, leftOperand, rightOperand));
            return reg;
        }

        String type = "and".equals(operator) ? "i1" : "i32";
        emitLine(String.format("%s = %s %s %s, %s", reg, operator, type, leftOperand, rightOperand));
        return reg;
    }

    public String emitLvalueAddressOf(Class class_, String id) throws Exception {
        // Return register containing address of lvalue
        String reg = variableToRegister.get(id);
        if (reg != null)
            // Is a local variable or parameter
            return reg;
                
        // Is instance variable
        reg = newRegister();
        emitLine(String.format("%s = getelementptr i8, i8* %%this, i32 %d", reg, class_.getOffset(id)));
        String type = class_.getField(id).varType;
        
        String ret = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to %s*", ret, reg, typeToLLVM(type)));

        // TODO: array assignment
        return ret;
    }

    public String emitRvalue(Class class_, String type, String id) throws Exception {
        String llvmType = typeToLLVM(type);
        // Return register containing value
        String reg = variableToRegister.get(id);
        if (reg != null) {
            // Is a local variable or parameter
            String ret = newRegister();
            emitLine(String.format("%s = load %s, %s* %s", ret, llvmType, llvmType, reg));
            return ret;
        }
                
        // Is instance variable
        String tmp = newRegister();
        emitLine(String.format("%s = getelementptr i8, i8* %%this, i32 %d", tmp, class_.getOffset(id)));

        String tmp_ = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to %s*", tmp_, tmp, llvmType));

        String ret = newRegister();
        emitLine(String.format("%s = load %s, %s* %s", ret, llvmType, llvmType, tmp_));
        // TODO: array elements

        return ret;
    }

    public void emitAssignment(String type, String lhs_reg, String value_reg) throws IOException {
        emitLine(String.format("store %s %s, %s* %s\n", typeToLLVM(type), value_reg, typeToLLVM(type), lhs_reg));
    }

    public String emitObjectAllocation(String type, int size) throws Exception {
        String allocReg = newRegister();
        emitLine(String.format("%s = call i8* @calloc(i32 1, i32 %d)", allocReg, size));

        String tmp = newRegister();
        int vt_size = classToVtable.get(type).size();
        emitLine(String.format("%s = bitcast i8* %s to [%d x i8*]**", tmp, allocReg, vt_size));
        emitLine(String.format("store [%d x i8*]* @.%s_vtable, [%d x i8*]** %s", vt_size, type, vt_size, tmp));

        return allocReg;
    }

    public VTable emitVTable(Class c) throws IOException {
        VTable vt = new VTable();
        StringBuilder methodDecls = new StringBuilder();
        int methodCount = buildMethodDecls(c, methodDecls, vt);
        emitLine(String.format("@.%s_vtable = global [%d x i8*] [%s]\n", c.name, methodCount, methodDecls));
        return vt;
    }

    public void emitPrintInt(String reg) throws IOException {
        emitLine(String.format("call void @print_int(i32 %s)", reg));
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
        return switch (type) {
            case "int" -> "i32";
            case "int[]" -> "i32*";
            case "boolean" -> "i1";
            case "boolean[]" -> "i1*";
            case "void" -> "void";
            default -> "i8*";
        };
    }

    private int buildMethodDecls(Class c, StringBuilder methodDecls, VTable vtable) {
        int cnt = 0;
        for (Method m : c.getMethods()) {
            if (vtable.get(m.id) != null)
                continue;

            vtable.put(m.id, cnt++);

            var args = new StringBuilder();
            for (Variable a : m.parameters)
                args.append(String.format(", %s", typeToLLVM(a.varType)));

            // All methods have a 'this' pointer as their first argument
            String signature = String.format("%s (i8*%s)", typeToLLVM(m.returnType), args.toString()); 
            String decl = String.format("i8* bitcast (%s* @%s_%s to i8*)", signature, c.inheritedFrom(m.id), m.id);
            if (methodDecls.length() > 0) methodDecls.append(", ");
            methodDecls.append(decl);
        }
        return cnt;
    }
}
