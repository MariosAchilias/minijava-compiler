import java.util.ArrayList;

public class Method extends Symbol{
    public String returnType;
    public ArrayList<Variable> parameters;
    private Scope localVars;
    private final String className;

    public Method(String returnType, String methodName, ArrayList<Variable> parameters, String className) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.className = className;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
        localVars = new Scope(SymbolTable.getInstance().getClass(className).getScope());
    }

    public String getClassName() {
        return className;
    }

    public Scope getLocalScope() {
        return localVars;
    }

    public static boolean compatibleSignatures(Method method, Method method_) {
        if (!method.returnType.equals(method_.returnType))
            return false;
        if (method.parameters.size() != method_.parameters.size())
            return false;
        for (int i = 0; i < method.parameters.size(); i++)
            if (method.parameters.get(i).varType != method_.parameters.get(i).varType)
                return false;

        return true;
    }
    public void prettyPrint() {
        System.out.print(returnType + " " + id + " (");
        for (Variable param: parameters) {
            System.out.print(param.varType + ", ");
        }
        System.out.println(")");
    }
}