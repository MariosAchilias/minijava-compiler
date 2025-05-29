import java.util.LinkedHashMap;

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

    public java.util.Collection<T> getValues() {
        return map.values();
    }

    public boolean hasSymbol(String id) {
        return map.containsKey(id);
    }

    public T getSymbol(String id) {
        return map.get(id);
    }

    public boolean addSymbol(String id, T symbol) {
        return map.put(id, symbol) == null;
    }

    public void prettyPrint() {
        for (T s: map.values()) {
            s.prettyPrint();
        }
    }

    public void printOffsets() {
        for (T s: map.values()) {
                s.printOffsets();
        }
    }
}
