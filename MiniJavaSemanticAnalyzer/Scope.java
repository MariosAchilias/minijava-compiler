import java.util.HashMap;

public class Scope {
    private Scope parent;
    private HashMap<String, Symbol> map;

    public Scope(Scope parentScope) {
        parent = parentScope;
    }

    public Scope getParent() {
        return parent;
    }

    public boolean has_symbol(String id) {
        return map.containsKey(id);
    }

    public Symbol get_symbol(String id) {
        return map.get(id);
    }

    public boolean add_symbol(String id, Symbol symbol) {
        return map.put(id, symbol) == null;
    }

    public void prettyPrint() {
        for (Symbol s: map.values()) {
            s.prettyPrint();
        }
    }
}
