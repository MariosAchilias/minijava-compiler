import java.util.HashMap;

public class Class extends Symbol {
    private HashMap<String, Method> methods;
    private HashMap<String, Variable> fields;

    public Class (String className) {
        super(SymbolType.CLASS, className);
    }

    public boolean addField(Variable field) {
        return fields.put(field.id, field) == null;
    }

    public boolean addMethod(Method method) {
        return methods.put(method.id, method) == null;
    }

    public void prettyPrint() {
        System.out.println("Class: " + id);
        System.out.println("Methods: ");
        for (Method m : methods.values()) {
            m.prettyPrint();
        }
        for (Variable f : fields.values()) {
            f.prettyPrint();
        }
    }

    public void printOffsets() {
        // TODO
    }

}
