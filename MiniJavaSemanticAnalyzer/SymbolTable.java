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
        current = current.getParent();
    }

    public Symbol get_symbol(String id, Symbol symbol) {
        for (Scope s = current; s != null; s = s.getParent()) {
            if (s.has_symbol(id)) {
                return s.get_symbol(id);
            }
        }
        return null;
    }

    public boolean add_symbol(String id, Symbol symbol) {
        return current.add_symbol(id, symbol);
    }

    public void prettyPrint() {
        Scope topScope = current;
        while (topScope.getParent() != null) {
            topScope = topScope.getParent();
        }
        topScope.prettyPrint();
    }

}
