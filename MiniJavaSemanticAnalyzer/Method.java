public class Method extends Symbol{
    public VarType returnType;
    public Variable[] parameters;
    public Method(VarType returnType, String methodName, Variable[] parameters) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.parameters = parameters;
    }
}