public class Variable extends Symbol {
    public VarType varType;
    public Variable(VarType type, String name) {
        super(SymbolType.FIELD_VAR, name);
        varType = type;
    }

    public void prettyPrint() {
        System.out.println(varType.toString() + " " + id);
    }
}