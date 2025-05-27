import java.util.HashMap;
import java.util.LinkedHashMap;

public final class SymbolTable {
    private static SymbolTable instance;
    private Scope current;

    private SymbolTable() {
        current = new Scope(null);
    }

    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    public void enterScope() {
        current = new Scope(current);
    }

    public void exitScope() {
        current = current.getParent();
    }

    public Symbol getSymbol(String id, Symbol symbol) {
        for (Scope s = current; s != null; s = s.getParent()) {
            if (s.hasSymbol(id)) {
                return s.getSymbol(id);
            }
        }
        return null;
    }

    public boolean addSymbol(String id, Symbol symbol) {
        return current.addSymbol(id, symbol);
    }

    public void prettyPrint() {
        Scope topScope = current;
        while (topScope.getParent() != null) {
            topScope = topScope.getParent();
        }
        topScope.prettyPrint();
    }

}
