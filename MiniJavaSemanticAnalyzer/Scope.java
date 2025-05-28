import java.util.LinkedHashMap;

public class Scope {
    private Scope parent;
    private LinkedHashMap<String, Symbol> map;

    public Scope(Scope parentScope) {
        map = new LinkedHashMap<String, Symbol>();
        parent = parentScope;
    }

    public Scope getParent() {
        return parent;
    }

    public boolean hasSymbol(String id) {
        return map.containsKey(id);
    }

    public Symbol getSymbol(String id) {
        return map.get(id);
    }

    public boolean addSymbol(String id, Symbol symbol) {
        return map.put(id, symbol) == null;
    }

    public void prettyPrint() {
        for (Symbol s: map.values()) {
            s.prettyPrint();
        }
    }

    public void printOffsets() {
        for (Symbol s: map.values()) {
            if (s.type == SymbolType.CLASS)
                ((Class) s).printOffsets();
        }
    }
}
