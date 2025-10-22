package SymbolTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public final class SymbolTable {
    public ArrayList<Class> classes;
    public Class main;
    private Class currentClass = null;
    private Method currentMethod = null;

    public SymbolTable() {
        classes = new ArrayList<Class>();
        main = null;
    }

    public void enterClass(Class c) {
        currentClass = c;
    }

    public void exitClass() {
        currentClass = null;
    }

    public void enterMethod(Method m) {
        currentMethod = m;
    }

    public void exitMethod() {
        currentMethod = null;
    }

    public void addVarOrField(Variable v) {
        if (currentMethod != null)
            currentMethod.localVars.add(v);
        else
            currentClass.fields.add(v);
    }

    public Variable getVarOrField(String name) {
        if (currentMethod == null)
            return currentClass.getField(name);
        
        Variable var = currentMethod.localVars.stream()
                .filter(v -> v.name.equals(name))
                .findFirst()
                .orElse(null);

        if (var != null)
            return var;

        var = currentMethod.parameters.stream()
                .filter(v -> v.name.equals(name))
                .findFirst()
                .orElse(null);

        if (var != null)
            return var;

        return currentClass.getField(name);
    }

    public Class getClass(String name) {
        if (name.equals(main.name))
            return main;
        return classes.stream()
                .filter(c -> c.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isSubclass(Class derived, Class base) {
        for (Class c = derived; c != null; c = c.superClass) {
            if (base.equals(c.name))
                return true;
        }
        return false;
    }

    public boolean compatibleMethodParameters(ArrayList<Variable> methodParameters, ArrayList<Variable> testParameters) throws Exception {
        if (methodParameters.size() != testParameters.size())
            return false;
        for (int i = 0; i < methodParameters.size(); i++) {
            String paramType = methodParameters.get(i).type;
            String testParamType = testParameters.get(i).type;
            if (Variable.isBuiltin(paramType)) {
                if (!paramType.equals((testParamType))) return false;
                else continue;
            }

            if (!isSubclass(getClass(testParamType), getClass(paramType)))
                return false;
        }

        return true;
    }
}
