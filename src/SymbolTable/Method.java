package SymbolTable;

import java.util.ArrayList;

public class Method extends Symbol{
    public String returnType;
    public ArrayList<Variable> parameters;
    private final Scope<Variable> localVars;

    public Method(String returnType, String methodName, ArrayList<Variable> parameters, Scope<Variable> classScope) {
        super(SymbolType.METHOD, methodName);
        this.returnType = returnType;
        this.parameters = parameters == null ? new ArrayList<Variable>() : parameters;
        this.localVars = new Scope<Variable>(classScope);
    }

    public Scope<Variable> getLocalScope() {
        return localVars;
    }

    public void prettyPrint() {
        System.out.print(returnType + " " + id + " (");
        for (Variable param: parameters) {
            System.out.print(param.varType + ", ");
        }
        System.out.println(")");
    }
}