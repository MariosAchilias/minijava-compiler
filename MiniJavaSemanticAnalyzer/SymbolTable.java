import java.util.ArrayList;

public final class SymbolTable {
    private final Scope<Class> classesScope;
    private final Scope<Method> methodScope;
    private final Scope<Variable> mainScope;
    private Scope<Variable> localScope;

    public SymbolTable() {
        classesScope = new Scope<Class>(null);
        methodScope = new Scope<Method>(null);
        mainScope = new Scope<Variable>(null);
        localScope = null;
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

    public Variable getLocalInnermost(String id) { return localScope.getSymbolInnermost(id); }

    public boolean addMethod(String id, String className, Method method) {
        return methodScope.addSymbol(id + ";" + className, method);
    }

    public Method getMethodLocal(String id, String className) {
        return methodScope.get(id + ";" + className);
    }

    public Method getMethod(String id, String className) {
        for (Class c = getClass(className); c != null; c = c.getParent()) {
            Method method = getMethodLocal(id, c.id);
            if (method != null)
                return method;
        }
        return null;
    }

    public boolean isSubclass(String derived, String base) {
        for (Class c = getClass(derived); c != null; c = c.getParent()) {
            if (base.equals(c.id))
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

            if (!isSubclass(paramType, testParamType))
                return false;
        }

        return true;
    }

    public Scope<Method> getMethodScope() {
        return methodScope;
    }

    public Scope<Variable> getMainScope() {
        return mainScope;
    }

    public void prettyPrint() {
        classesScope.prettyPrint();
    }

    public void printOffsets() {
        for (Class c: classesScope.getValues())
            c.printOffsets(this);
    }

}
