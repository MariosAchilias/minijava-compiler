import java.util.ArrayList;

public class Method extends Symbol{
    public String returnType;
    public ArrayList<Variable> parameters;
    private final Scope<Variable> localVars;
    private final String className;

    public Method(String returnType, String methodName, ArrayList<Variable> parameters, Scope classScope, String className) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.className = className;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
        this.localVars = new Scope<Variable>(classScope);
    }

    public String getClassName() {
        return className;
    }

    public Scope<Variable> getLocalScope() {
        return localVars;
    }

    public void prettyPrint() {
        System.out.print(returnType + " " + id + " (");
        for (Variable param: parameters) {
            System.out.print(param.varType + ", ");
        }
        System.out.println(")");
    }
}