import java.util.ArrayList;

public class Method extends Symbol{
    public String returnType;
    public ArrayList<Variable> parameters;
    private Scope localVars;
    public Method(String returnType, String methodName, ArrayList<Variable> parameters, Scope classScope) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
        localVars = new Scope(classScope);
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