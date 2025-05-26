import java.util.HashMap;
import java.util.LinkedHashMap;

public class SymbolTable {
    private Scope current;

    public SymbolTable() {
        current = new Scope(null);
    }

    public void enterScope() {
        current = new Scope(current);
    }

    public void exitScope() {
        current = current.parent;
    }

    public void prettyPrint() {
        // Only top-level scope (which contains classes) is intended for printing
        if (current.parent != null)
            return;

        for (Symbol s: current.map.values()) {
            s.prettyPrint();
        }
    }

}
