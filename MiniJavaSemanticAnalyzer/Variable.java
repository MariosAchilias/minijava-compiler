public class Variable extends Symbol {
    public String varType;
    public Variable(String type, String name) {
        super(SymbolType.FIELD_VAR, name);
        varType = type;
    }

    public void prettyPrint() {
        System.out.println(varType + " " + id);
    }
}