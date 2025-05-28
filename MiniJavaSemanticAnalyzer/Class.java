import java.util.LinkedHashMap;

public class Class extends Symbol {
    private LinkedHashMap<String, Method> methods;
    private LinkedHashMap<String, Variable> fields;
    private Class superClass;
    private Scope scope;

    public Class (String className, Class superClass, Scope parentScope) {
        super(SymbolType.CLASS, className);
        methods = new LinkedHashMap<String, Method>();
        fields = new LinkedHashMap<String, Variable>();
        this.scope = new Scope(parentScope);
        this.superClass = superClass;
    }

    public Scope getScope() {
        return scope;
    }

    public Class getParent() {return superClass;}

    public boolean addField(Variable field) {
        return fields.put(field.id, field) == null;
    }

    public Variable getField(String id) {return fields.get(id);}

    public boolean addMethod(Method method) {
        return methods.put(method.id, method) == null;
    }

    public Method getMethod(String id) {return methods.get(id);}

    public void prettyPrint() {
        System.out.println("Class: " + id);
        System.out.println("Fields: ");
        for (Variable f : fields.values()) {
            f.prettyPrint();
        }
        System.out.println("Methods: ");
        for (Method m : methods.values()) {
            m.prettyPrint();
        }
    }

    private int getFieldsOffset() {
        int offset = superClass == null ? 0 : superClass.getFieldsOffset();
        for (Variable f : fields.values()) {
            switch (f.varType) {
                case "int":
                    offset += 4;
                    break;
                case "boolean":
                    offset += 1;
                    break;
                default:
                    offset += 8;
            }
        }
        return offset;
    }

    private int getMethodsOffset() {
        int offset = superClass == null ? 0 : superClass.getMethodsOffset();
        for (Method m: methods.values()) {
            offset += 8;
        }
        return offset;
    }

    public void printOffsets() {
        int offset = superClass == null ? 0 : superClass.getFieldsOffset();
        for (Variable f : fields.values()) {
            System.out.println(id + "." + f.id + " : " + offset);
            switch (f.varType) {
                case "int":
                    offset += 4;
                    break;
                case "boolean":
                    offset += 1;
                    break;
                default:
                    offset += 8;
            }
        }
        offset = superClass == null ? 0 : superClass.getMethodsOffset();
        for (Method m : methods.values()) {
            System.out.println(id + "." + m.id + " : " + offset);
            offset += 8;
        }
    }

}
