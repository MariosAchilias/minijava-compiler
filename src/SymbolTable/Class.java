package SymbolTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Class {
    public final String name;
    public final Class superClass;
    public ArrayList<Variable> fields;
    public ArrayList<Method> methods;
    public ArrayList<Method> vtable;

    public Class (String name, Class superClass) {
        this.name = name;
        this.superClass = superClass;
        this.methods = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.vtable = new ArrayList<>();
    }

    public int getOffset(String field) {
        // First 8 bytes in memory are Vtable pointer
        int offset = superClass == null ? 8 : superClass.getOffset(field);
        if (offset != -1)
            return offset;
        
        for (Variable v: fields) {
            if (v.name.equals(field))
                return offset;

            offset += sizeOf(v.type);
        }
        return -1;
    }

    public ArrayList<Method> getMethods () {
        ArrayList<Method> methods = new ArrayList<>();
        for (Class c = this; c != null; c = c.superClass) {
            methods.addAll(c.methods);
        }
        return methods;
    }

    public Method getMethod (String name) {
        for (Class c = this; c != null; c = c.superClass) {
            Method m = c.methods.stream()
                    .filter(x -> x.name.equals(name))
                    .findFirst()
                    .orElse(null);

            if (m != null)
                return m;
        }
        return null;
    }

    public Class inheritedFrom (Method m) {
        for (Class c = this; c != null; c = c.superClass) {
            if (c.methods.stream().anyMatch(x -> x.name.equals(m.name)))
                return c;
        }
        return null;
    }

    public Variable getField(String name) {
        for (Class c = this; c != null; c = c.superClass) {
            Variable var = fields.stream()
                    .filter(v -> v.name.equals(name))
                    .findFirst()
                    .orElse(null);

            if (var != null)
                return var;
        }
        return null;
    }

    public int getSize() {
        // first 8 bytes hold vtable pointer
        int size = superClass == null ? 8 : superClass.getSize();
        for (Variable v: fields) {
            size += sizeOf(v.type);
        }
        return size;
    }

    private static int sizeOf(String type) {
        return switch(type) {
            case "int"      -> 4;
            case "boolean"  -> 1;
            case "int[]"    -> 8;
            case "boolean[]"-> 8;
            default         -> -1;
        };
    }

}
