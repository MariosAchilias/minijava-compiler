package SymbolTable;

public class Symbol {
    public SymbolType type;
    public String id;
    public Symbol(SymbolType type, String id) {
        this.type = type;
        this.id = id;
    }
    public void prettyPrint() {
        System.out.println(type.toString() + " " + id);
    };
}