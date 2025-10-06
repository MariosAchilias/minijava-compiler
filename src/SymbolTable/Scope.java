package SymbolTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Scope<T extends Symbol> {
    private final Scope<T> parent;
    private final LinkedHashMap<String, T> map;

    public Scope(Scope<T> parentScope) {
        map = new LinkedHashMap<String, T>();
        parent = parentScope;
    }

    public T get(String id) {
        return map.get(id);
    }

    public Scope<T> getParent() {
        return parent;
    }

    public ArrayList<T> getValues() {
        return new ArrayList<T>(map.values());
    }

    public T getSymbol(String id) {
        for (Scope<T> s = this; s != null; s = s.parent) {
            T symbol = s.map.get(id);
            if (symbol != null)
                return symbol;
        }
        return null;
    }

    public T getLocalSymbol(String id) {
        return map.get(id);
    }

    public boolean addSymbol(String id, T symbol) {
        return map.put(id, symbol) == null;
    }
}
