import java.util.HashMap;

public class Scope {
    public Scope parent;
    public HashMap<String, Symbol> map;
    public Scope(Scope parentScope) {
        parent = parentScope;
    }
    public Symbol get_symbol(String s) {
        // check all scopes up hierarchy
        return null;
    }
    public void add_symbol(String id, Symbol symbol) {

    }
}
