package SymbolTable;

public class Variable {
    public String name;
    public String type;
    public String register;
    public Variable(String type, String name) {
        this.name = name;
        this.type = type;
    }

    public static boolean isBuiltin(String type) {
        return type.equals("int") || type.equals("boolean") || type.equals("int[]") || type.equals("boolean[]");
    }
}