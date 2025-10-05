package SymbolTable;

import java.util.LinkedHashMap;

public class Class {
    public final String name;
    private final Class superClass;
    private Scope<Variable> fields;
    private Scope<Method> methods;

    public Class (String name, Class superClass) {
        this.name = name;
        this.superClass = superClass;
        this.methods = new Scope<Method>(superClass == null ? null : superClass.getMethodScope());
        this.fields = new Scope<Variable>(superClass == null ? null : superClass.getVariableScope());
    }

    public Scope<Variable> getVariableScope() {
        return fields;
    }
    public Scope<Method> getMethodScope() {
        return methods;
    }

    public Class getParent() {return superClass;}

    public Method getLocalMethod (String name) {
        return methods.get(name);
    }

    public Method getMethod (String name) {
        for (Class c = this; c != null; c = c.superClass) {
            Method m = c.getLocalMethod(name);
            if (m != null)
                return m;
        }
        return null;
    }

    public boolean addMethod (Method method) {
        return methods.addSymbol(method.id, method);
    }

    public boolean addField(Variable field) {
        return fields.addSymbol(field.id, field);
    }

    public Variable getField(String id) {return fields.get(id);}

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

//    private int getMethodsOffset(SymbolTable symbolTable) {
//        int offset = superClass == null ? 0 : superClass.getMethodsOffset(symbolTable);
//        for (Method m: symbolTable.getMethodScope().getValues()) {
//            if (m.getClassName().equals(id))
//                offset += 8;
//        }
//        return offset;
//    }

   public void printOffsets(SymbolTable symbolTable) {
       int offset = superClass == null ? 0 : superClass.getFieldsOffset();
       for (Variable f : fields.getValues()) {
           System.out.println(name + "." + f.id + " : " + offset);
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
       // TODO: add printing method offsets using vtable
    }

}
