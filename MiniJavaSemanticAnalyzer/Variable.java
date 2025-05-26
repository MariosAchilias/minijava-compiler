public class Variable extends Symbol {
    public VarType type;
    public Variable(VarType type, String name) {
        super(SymbolType.FIELD_VAR, name);
        this.type = type;
    }
}