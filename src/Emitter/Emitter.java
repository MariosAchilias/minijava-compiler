package Emitter;

import SymbolTable.*;
import SymbolTable.Class;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Emitter {
    FileOutputStream outFile;
    private int registerCount = 0;
    private int labelCount = 0;
    private int indentation = 0;
    public Emitter(FileOutputStream outFile) {
        this.outFile = outFile;
    }

    public void methodStart(Class c, Method m) throws IOException {
        var args = new StringBuilder();
        for (Variable a : m.parameters) {
            args.append(String.format(", %s %%%s", typeToLLVM(a.type), a.name));
        }

        if (m.name.equals("main")) {
            emitLine(String.format("\ndefine void @main() {\n"));
        } else {
            // All methods have a 'this' pointer as their first argument
            emitLine(String.format("\ndefine %s @%s_%s(i8* %%this %s) {\n", typeToLLVM(m.returnType), c.name, m.name, args.toString()));
        }
        indentation++;

        // Allocate parameters & local variables
        for (Variable a : Stream.concat(m.localVars.stream(), m.parameters.stream()).collect(Collectors.toList())) {
            String type = typeToLLVM(a.type);
            String reg = newRegister();
            a.register = reg;
            emitLine(String.format("%s = alloca %s", reg, type));
            // For parameters, initialize to given values
            if (m.parameters.contains(a))
                emitLine(String.format("store %s %s, %s* %s\n", type, "%" + a.name, type, reg));
        }
    }

    public void methodEnd(Method m, String ret) throws IOException {
        emitLine(String.format("ret %s %s", typeToLLVM(m.returnType), ret));
        outFile.write("}\n".getBytes());
        for (Variable a : Stream.concat(m.localVars.stream(), m.parameters.stream()).collect(Collectors.toList())) {
            a.register = null;
        }
        indentation--;
    }

    public String call(String objectReg, Class c, Method m, java.util.ArrayList<String> args) throws Exception {
        String ret = newRegister();
        int vtOffset = IntStream.range(0, c.vtable.size())
                .filter(i -> c.vtable.get(i).name.equals(m.name))
                .findFirst()
                .orElse(-1);

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
            argTypes.append(String.format(", %s", typeToLLVM(p.type)));
        }

        String retType = typeToLLVM(m.returnType);
        String funcPtr = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to %s(%s)*", funcPtr, tmp_, retType, argTypes));

        StringBuilder argRegs = new StringBuilder(String.format("i8* %s", objectReg)); // 'this'
        for (String a: args) {
            // a contains comma separated type and register
            String type = typeToLLVM(a.split(",")[0]);
            String reg = a.split(",")[1];
            argRegs.append(String.format(", %s %s", type, reg));
        }

        emitLine(String.format("%s = call %s %s(%s)", ret, retType, funcPtr, argRegs));

        return ret;
    }

    public String binaryOperation(String operator, String leftOperand, String rightOperand) throws IOException {
        String reg = newRegister();
        if (operator.equals("slt")) {
            emitLine(String.format("%s = icmp slt i32 %s, %s", reg, leftOperand, rightOperand));
            return reg;
        }

        String type = "and".equals(operator) ? "i1" : "i32";
        emitLine(String.format("%s = %s %s %s, %s", reg, operator, type, leftOperand, rightOperand));
        return reg;
    }

    public String not(String operand) throws IOException {
        String reg = newRegister();
        emitLine(String.format("%s = xor i1 %s, 1", reg, operand));
        return reg;
    }

    public String lvalueAddressOf(Class class_, Variable var) throws Exception {
        // Return register containing address of lvalue
        if (var.register != null)
            // Is a local variable or parameter
            return var.register;
                
        // Is instance variable
        String reg = newRegister();
        emitLine(String.format("%s = getelementptr i8, i8* %%this, i32 %d", reg, class_.getOffset(var.name)));
        String type = var.type;
        
        String ret = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to %s*", ret, reg, typeToLLVM(type)));

        return ret;
    }

    public void branch(String condReg, String labelIf, String labelElse) throws IOException {
        emitLine(String.format("br i1 %s, label %%%s, label %%%s", condReg, labelIf, labelElse));
    }

    public void branch(String label) throws IOException {
        emitLine(String.format("br label %%%s", label));
    }
    
    public void putLabel(String label) throws IOException {
        outFile.write((label + ":\n").getBytes());
    }

    public String rvalue(Class class_, Variable var) throws Exception {
        String llvmType = typeToLLVM(var.type);
        // Return register containing value
        String reg = var.register;
        if (reg != null) {
            // Is a local variable or parameter
            String ret = newRegister();
            emitLine(String.format("%s = load %s, %s* %s", ret, llvmType, llvmType, reg));
            return ret;
        }

        if (class_.getOffset(var.name) != -1) {
            // Is instance variable
            String tmp = newRegister();
            emitLine(String.format("%s = getelementptr i8, i8* %%this, i32 %d", tmp, class_.getOffset(var.name)));

            String tmp_ = newRegister();
            emitLine(String.format("%s = bitcast i8* %s to %s*", tmp_, tmp, llvmType));

            String ret = newRegister();
            emitLine(String.format("%s = load %s, %s* %s", ret, llvmType, llvmType, tmp_));
            return ret;
        }

        // Is temporary rvalue

        return null;
    }

    public void assignment(String type, String lhs_reg, String value_reg) throws IOException {
        emitLine(String.format("store %s %s, %s* %s\n", typeToLLVM(type), value_reg, typeToLLVM(type), lhs_reg));
    }

    public void arrayAssignment(String type, String arrReg, String idxReg, String valReg) throws IOException {
        // TODO: bounds check
        String idx = newRegister();
        // length stored in first 4 bytes (offset for i32 arrays is 1, for i1 (1-byte elements) is 4)
        int offset = baseType(typeToLLVM(type)).equals("i32") ? 1 : 4;
        emitLine(String.format("%s = add i32 %s, %d", idx, idxReg, offset));

        String type_ = baseType(typeToLLVM(type));
        String ptr = newRegister();
        emitLine(String.format("%s = getelementptr %s, %s* %s, i32 %s", ptr, type_, type_, arrReg, idx));
        emitLine(String.format("store %s %s, %s* %s", type_, valReg, type_, ptr));
    }

    public String arrayLookup(String type, String arrReg, String idxReg) throws IOException {
        String idx = newRegister();
        // length stored in first 4 bytes (offset for i32 arrays is 1, for i1 (1-byte elements) is 4)
        int offset = baseType(typeToLLVM(type)).equals("i32") ? 1 : 4;
        emitLine(String.format("%s = add i32 %s, %d", idx, idxReg, offset));

        String type_ = baseType(typeToLLVM(type));
        String ptr = newRegister();
        emitLine(String.format("%s = getelementptr %s, %s* %s, i32 %s", ptr, type_, type_, arrReg, idx));
        String val = newRegister();
        emitLine(String.format("%s = load %s, %s* %s", val, type_, type_, ptr));

        return val;
    }

    public String objectAllocation(Class c) throws Exception {
        String allocReg = newRegister();
        emitLine(String.format("%s = call i8* @calloc(i32 1, i32 %d)", allocReg, c.getSize()));

        String tmp = newRegister();
        int vt_size = c.vtable.size();
        emitLine(String.format("%s = bitcast i8* %s to [%d x i8*]**", tmp, allocReg, vt_size));
        emitLine(String.format("store [%d x i8*]* @.%s_vtable, [%d x i8*]** %s", vt_size, c.name, vt_size, tmp));

        return allocReg;
    }

    public String allocateIntArray(String size) throws Exception {
        // first element is array length
        String size_ = newRegister();
        emitLine(String.format("%s = add i32 %s, 1", size_, size));

        String tmp = newRegister();
        emitLine(String.format("%s = call i8* @calloc(i32 %s, i32 4)", tmp, size_));
        String ptr = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to i32*", ptr, tmp));
        emitLine(String.format("store i32 %s, i32* %s", size, ptr));

        return ptr;
    }

    public String allocateBoolArray(String size) throws Exception {
        // first 4 bytes are array length (i32)
        String size_ = newRegister();
        emitLine(String.format("%s = add i32 %s, 4", size_, size));

        String tmp = newRegister();
        emitLine(String.format("%s = call i8* @calloc(i32 %s, i32 1)", tmp, size_));

        String i32ptr = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to i32*", i32ptr, tmp));
        emitLine(String.format("store i32 %s, i32* %s", size, i32ptr));

        String ptr = newRegister();
        emitLine(String.format("%s = bitcast i8* %s to i1*", ptr, tmp));

        return ptr;
    }

    public String arrayLength(String ptr, String type) throws IOException {
        String type_ = typeToLLVM(type);
        if (type_ == "i1") {
            String tmp = newRegister();
            emitLine(String.format("%s = bitcast i1* %s to i32*", tmp, ptr));
            ptr = tmp;
        }

        String size = newRegister();
        emitLine(String.format("%s = load i32, i32* %s", size, ptr));
        return size;
    }

    public void VTable(Class c) throws IOException {
        StringBuilder methodDecls = new StringBuilder();
        c.vtable = buildMethodDecls(c, methodDecls, c.vtable);
        emitLine(String.format("@.%s_vtable = global [%d x i8*] [%s]\n", c.name, c.vtable.size(), methodDecls));
    }

    public void printInt(String reg) throws IOException {
        emitLine(String.format("call void @print_int(i32 %s)", reg));
    }

    public void helpers() throws IOException {
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

    public String newRegister () {
        return "%_" + registerCount++;
    }

    public String newLabel () {
        return "label_" + labelCount++;
    }

    private void emitLine(String line) throws IOException {
        String s = "\t".repeat(indentation) + line + "\n";
        outFile.write(s.getBytes());
    }

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

    private static String baseType(String type) {
        return switch (type) {
            case "i32*" -> "i32";
            case "i32" -> "i32";
            case "i1*" -> "i1";
            case "i1" -> "i1";
            case "void" -> "void";
            default -> "i8";
        };
    }


    private ArrayList<Method> buildMethodDecls(Class c, StringBuilder methodDecls, ArrayList<Method> vtable) {
        for (Method m : c.getMethods()) {
            if (vtable.stream().anyMatch(x -> x.name.equals(m.name)))
                continue;

            vtable.add(m);

            var args = new StringBuilder();
            for (Variable a : m.parameters)
                args.append(String.format(", %s", typeToLLVM(a.type)));

            // All methods have a 'this' pointer as their first argument
            String signature = String.format("%s (i8*%s)", typeToLLVM(m.returnType), args.toString()); 
            String decl = String.format("i8* bitcast (%s* @%s_%s to i8*)", signature, c.inheritedFrom(m).name, m.name);
            if (methodDecls.length() > 0) methodDecls.append(", ");
            methodDecls.append(decl);
        }
        return vtable;
    }
}
