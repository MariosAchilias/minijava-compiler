package SymbolTable;

import java.util.LinkedHashMap;

public class Class extends Symbol {
    private final Class superClass;
    private Scope<Variable> fields;

    public Class (String className, Class superClass) {
        super(SymbolType.CLASS, className);
        this.superClass = superClass;
        this.fields = new Scope<Variable>(superClass == null ? null : superClass.getScope());
    }

    public Scope<Variable> getScope() {
        return fields;
    }

    public Class getParent() {return superClass;}

    public boolean addField(Variable field) {
        return fields.addSymbol(field.id, field);
    }

    public Variable getField(String id) {return fields.get(id);}

    public void prettyPrint(SymbolTable symbolTable) {
        System.out.println("Class: " + id);
        System.out.println("Fields: ");
        fields.prettyPrint();
        System.out.println("Methods: ");
        for (Method m: symbolTable.getMethodScope().getValues()) {
            if (m.getClassName().equals(id))
                m.prettyPrint();
        }
    }

    private int getFieldsOffset() {
        int offset = superClass == null ? 0 : superClass.getFieldsOffset();
        for (Variable f : fields.getValues()) {
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

    private int getMethodsOffset(SymbolTable symbolTable) {
        int offset = superClass == null ? 0 : superClass.getMethodsOffset(symbolTable);
        for (Method m: symbolTable.getMethodScope().getValues()) {
            if (m.getClassName().equals(id))
                offset += 8;
        }
        return offset;
    }

    public void printOffsets(SymbolTable symbolTable) {
        // TODO. proper offsets for overriden methods
        int offset = superClass == null ? 0 : superClass.getFieldsOffset();
        for (Variable f : fields.getValues()) {
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
        offset = superClass == null ? 0 : superClass.getMethodsOffset(symbolTable);
        for (Method m : symbolTable.getMethodScope().getValues()) {
            if (!m.getClassName().equals(id))
                continue;
            System.out.println(id + "." + m.id + " : " + offset);
            offset += 8;
        }
    }

}
