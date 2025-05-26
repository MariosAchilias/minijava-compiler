public class Method extends Symbol{
    public VarType returnType;
    public Variable[] parameters;
    public Method(VarType returnType, String methodName, Variable[] parameters) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.parameters = parameters;
    }
    public void prettyPrint() {
        System.out.print(returnType.toString() + " " + id + " (");
        for (Variable param: parameters) {
            System.out.print(param.type.toString() + ", ");
        }
        System.out.println(")");
    }
}