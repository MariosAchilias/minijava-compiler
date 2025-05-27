import java.util.ArrayList;

public class Method extends Symbol{
    public VarType returnType;
    public ArrayList<Variable> parameters;
    public Method(VarType returnType, String methodName, ArrayList<Variable> parameters) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
    }
    public void prettyPrint() {
        System.out.print(returnType.toString() + " " + id + " (");
        for (Variable param: parameters) {
            System.out.print(param.varType.toString() + ", ");
        }
        System.out.println(")");
    }
}