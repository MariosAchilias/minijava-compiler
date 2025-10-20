package SymbolTable;

import java.util.ArrayList;

public class Method {
    public String name;
    public String returnType;
    public ArrayList<Variable> parameters;
    public ArrayList<Variable> localVars;

    public Method(String returnType, String name, ArrayList<Variable> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters == null ? new ArrayList<>() : parameters;
        this.localVars = new ArrayList<>();
    }
}