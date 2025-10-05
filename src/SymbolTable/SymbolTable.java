package SymbolTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public final class SymbolTable {
    private final LinkedHashMap<String, Class> classes;
    public Class main;
    private Scope<Variable> localVariables;

    public SymbolTable() {
        classes = new LinkedHashMap<String, Class>();
        main = null;
        localVariables = null;
    }

    public void enterClassScope(Class c) {
        localVariables = c.getVariableScope();
    }

    public void exitClassScope() {
        localVariables = null;
    }

    public void enterScope(Scope<Variable> scope) {
        localVariables = scope;
    }

    public void exitLocalScope() {
        localVariables = localVariables.getParent();
    }

    public boolean addVariable(String id, Variable symbol) {
        return localVariables.addSymbol(id, symbol);
    }

    public Variable getVar(String id) {
        return localVariables.getSymbol(id);
    }

    public Variable getLocalVar(String id) { return localVariables.getLocalSymbol(id); }

    public Method getMethod(String id, String className) {
        for (Class c = classes.get(className); c != null; c = c.getParent()) {
            Method method = c.getMethod(id);
            if (method != null)
                return method;
        }
        return null;
    }

    public Class getClass(String name) throws Exception {
        if (name == main.name)
            return main;
        return classes.get(name);
    }

    public Collection<Class> getClasses() {
        return classes.values();
    }

    public Class newClass(String name, Class superClass) {
        Class c = new Class(name, superClass);
        classes.put(name, c);
        return c;
    }

    public boolean isSubclass(String derived, String base) {
        for (Class c = classes.get(derived); c != null; c = c.getParent()) {
            if (base.equals(c.name))
                return true;
        }
        return false;
    }

    public boolean compatibleMethodParameters(ArrayList<Variable> methodParameters, ArrayList<Variable> testParameters) {
        if (methodParameters.size() != testParameters.size())
            return false;
        for (int i = 0; i < methodParameters.size(); i++) {
            String paramType = methodParameters.get(i).varType;
            String testParamType = testParameters.get(i).varType;
            if (Variable.isBuiltin(paramType)) {
                if (!paramType.equals((testParamType))) return false;
                else continue;
            }

            if (!isSubclass(testParamType, paramType))
                return false;
        }

        return true;
    }
}
