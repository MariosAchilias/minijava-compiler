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

    public Scope newScope() {
        return new Scope(current);
    }

    public void enterScope(Scope scope) {
        current = scope;
    }

    public void exitScope() {
        current = current.getParent();
    }

    public Scope getCurrScope() {
        return current;
    }

    public Symbol getSymbol(String id) {
        for (Scope s = current; s != null; s = s.getParent()) {
            if (s.hasSymbol(id)) {
                return s.getSymbol(id);
            }
        }
        return null;
    }

    public Symbol getLocal(String id) {
        return current.getSymbol(id);
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

    public void printOffsets() {
        Scope topScope = current;
        while (topScope.getParent() != null) {
            topScope = topScope.getParent();
        }
        topScope.printOffsets();
    }

}
