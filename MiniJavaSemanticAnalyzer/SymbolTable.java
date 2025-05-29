public final class SymbolTable {
    private static SymbolTable instance;
    private final Scope<Class> classesScope;
    private final Scope<Method> methodScope;
    private Scope<Variable> localScope;

    private SymbolTable() {
        classesScope = new Scope<Class>(null);
        methodScope = new Scope<Method>(null);
        localScope = null;
    }

    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    public void enterScope(Scope<Variable> scope) {
        localScope = scope;
    }

    public void exitLocalScope() {
        localScope = localScope.getParent();
    }

    public boolean addClass(String id, Class class_) {
        return classesScope.addSymbol(id, class_);
    }

    public Class getClass(String id) {
        return classesScope.get(id);
    }

    public boolean addLocal(String id, Variable symbol) {
        return localScope.addSymbol(id, symbol);
    }

    public Variable getLocal(String id) {
        return localScope.getSymbol(id);
    }

    public boolean addMethod(String id, String className, Method method) {
        return methodScope.addSymbol(id + "_" + className, method);
    }

    public Method getMethod(String id, String className) {
        for (Class c = getClass(className); c != null; c = c.getParent()) {
            String name = c.id;
            Method method = methodScope.get(id + "_" + name);
            if (method != null)
                return method;
        }
        return null;
    }

    public Scope<Method> getMethodScope() {
        return methodScope;
    }

    public void prettyPrint() {
        classesScope.prettyPrint();
    }

    public void printOffsets() {
        for (Class c: classesScope.getValues())
            c.printOffsets();
    }

}
