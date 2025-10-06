package SymbolTable;

import java.util.ArrayList;

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

    public ArrayList<Method> getMethods () {
        return methods.getValues();
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

    public int getSize() {
        int size = superClass != null ? superClass.getSize() : 0;
        size += fields.getValues().size();
        for (Method m: methods.getValues()) {
            boolean isOverride = (this.getLocalMethod(m.id) != null) && (superClass.getMethod(m.id) != null);
            if (!isOverride)
                size += 8;
        }
        return size;
    }

}
