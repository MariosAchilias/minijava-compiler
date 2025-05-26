import java.util.HashMap;

public class Class extends Symbol {
    private HashMap<String, Method> methods;
    private HashMap<String, Variable> fields;

    public Class (String className) {
        super(SymbolType.CLASS, className);
    }

    public boolean addField(Variable field) {
        return true;
    }

    public boolean addMethod(Method method) {
        return true;
    }

}
