package SymbolTable;

public class Variable extends Symbol {
    public String varType;
    public Variable(String type, String name) {
        super(SymbolType.FIELD_VAR, name);
        varType = type;
    }

    public static boolean isBuiltin(String type) {
        return type.equals("int") || type.equals("boolean") || type.equals("int[]") || type.equals("boolean[]");
    }

    public void prettyPrint() {
        System.out.println(varType + " " + id);
    }
}