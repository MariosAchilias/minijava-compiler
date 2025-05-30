import java.util.ArrayList;

public class Method extends Symbol{
    public String returnType;
    public ArrayList<Variable> parameters;
    private final Scope<Variable> localVars;
    private final String className;

    public Method(String returnType, String methodName, ArrayList<Variable> parameters, String className) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.className = className;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
        this.localVars = new Scope<Variable>(SymbolTable.getInstance().getClass(className).getScope());
    }

    public String getClassName() {
        return className;
    }

    public Scope<Variable> getLocalScope() {
        return localVars;
    }

    public static boolean compatibleParameters(ArrayList<Variable> methodParameters, ArrayList<Variable> testParameters) {
        if (methodParameters.size() != testParameters.size())
            return false;
        for (int i = 0; i < methodParameters.size(); i++) {
            String type = methodParameters.get(i).varType;
            // if parameter is of user-defined type, check if given parameter is a subtype
            if (!(type.equals("int") || type.equals("boolean") || type.equals("int[]") || type.equals("boolean[]"))) {
                SymbolTable st = SymbolTable.getInstance();
                return Class.isSubtype(st.getClass(type), st.getClass(testParameters.get(i).varType));
            }

            if (!methodParameters.get(i).varType.equals(testParameters.get(i).varType))
                return false;

        }

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