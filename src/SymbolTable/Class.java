package SymbolTable;

import java.util.Collection;

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

    public Collection<Method> getMethods () {
        Collection<Method> res = methods.getValues();
        for (Class c = this.superClass; c != null; c = c.superClass)
            res.addAll(c.methods.getValues());
        return res;
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

}
