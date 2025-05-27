import java.util.LinkedHashMap;

public class Class extends Symbol {
    private LinkedHashMap<String, Method> methods;
    private LinkedHashMap<String, Variable> fields;
    private Class superClass;

    public Class (String className, Class superClass) {
        super(SymbolType.CLASS, className);
        methods = new LinkedHashMap<String, Method>();
        fields = new LinkedHashMap<String, Variable>();
    }

    public boolean addField(Variable field) {
        return fields.put(field.id, field) == null;
    }

    public boolean addMethod(Method method) {
        return methods.put(method.id, method) == null;
    }

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

    private int printOffsets_aux(int start) {
        int offset = 0;
        for (Variable f : fields.values()) {
            System.out.println(id + "." + f.id + " : " + (start + offset));
            switch (f.varType) {
                case INTEGER:
                    offset += 4;
                    break;
                case BOOLEAN:
                    offset += 1;
                    break;
                default:
                    offset += 8;
            }
        }
        for (Method m: methods.values()) {
            System.out.println(id + "." + m.id + " : " + (start + offset));
            offset += 8;
        }
        return start + offset;
    }

    public void printOffsets() {
        int superEnd = superClass == null ? 0 : superClass.printOffsets_aux(0);
        printOffsets_aux(superEnd);
    }

}
