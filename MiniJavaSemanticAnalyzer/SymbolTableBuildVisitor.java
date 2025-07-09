import syntaxtree.*;
import visitor.*;

class SymbolTableBuildVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    public SymbolTableBuildVisitor() {
        symbolTable = SymbolTable.getInstance();
    }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        symbolTable.enterScope(symbolTable.getMainScope());
        n.f14.accept(this, null);
        symbolTable.exitLocalScope();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        n.f0.accept(this, null);

        String className = n.f1.accept(this, null);
        if (symbolTable.getClass(className) != null)
            throw new SemanticException("Double declaration of class '" + className + "'");

        Class classSymbol = new Class(className, null);
        symbolTable.addClass(className, classSymbol);
        symbolTable.enterScope(classSymbol.getScope());

        n.f2.accept(this, null);
        n.f3.accept(this, className);
        n.f4.accept(this, className);
        n.f5.accept(this, null);

        symbolTable.exitLocalScope();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        n.f0.accept(this, null);

        String className = n.f1.accept(this, null);
        if (symbolTable.getClass(className) != null)
            throw new SemanticException("Double declaration of class '" + className + "'");

        String superClassName = n.f3.accept(this, null);
        Class superClass = symbolTable.getClass(superClassName);
        if (superClass == null)
            throw new SemanticException("Class '" + className + "' extends undeclared class '" + superClassName + "'");

        Class classSymbol = new Class(className, superClass);

        symbolTable.addClass(className, classSymbol);

        symbolTable.enterScope(classSymbol.getScope());

        n.f5.accept(this, className);
        n.f6.accept(this, className);

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

        if (symbolTable.getLocalInnermost(name) != null)
            throw new SemanticException("Double definition of variable " + name);

        Variable var = new Variable(type, name);
        symbolTable.addLocal(name, var);
        Class class_ = symbolTable.getClass(argu);
        if (class_ != null) {
            class_.addField(var);
        }

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String className) throws Exception {
        String name = n.f2.accept(this, null);
        if (symbolTable.getMethodLocal(name, className) != null)
            throw new SemanticException("Double declaration of method '"+ name + "'");

        String retType = n.f1.accept(this, null);
        Method method = new Method(retType, name, null, className);

        symbolTable.addMethod(name, className, method);

        symbolTable.enterScope(method.getLocalScope());

        // FormalParameterList visitor should create and set array
        if (n.f4.present())
            n.f4.accept(this, className + ";" + name);


        n.f7.accept(this, null);

        symbolTable.exitLocalScope();

        // not overriding
        if (symbolTable.getClass(className).getParent() == null || symbolTable.getMethod(name, symbolTable.getClass(className).getParent().id) == null)
            return null;

        Method overriden = symbolTable.getMethod(name, symbolTable.getClass(className).getParent().id);
        assert overriden != null;
        if (!method.returnType.equals(overriden.returnType))
            throw new SemanticException("Method override has different return type");
        if (!symbolTable.compatibleMethodParameters(method.parameters, overriden.parameters))
            throw new SemanticException("Method override has incompatible argument types");

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        String className = argu.split(";")[0];
        String methodName = argu.split(";")[1];
        Method method = symbolTable.getMethod(methodName, className);
        Variable param = new Variable(type, name);
        assert method != null;
        for (Variable v: method.parameters) {
            if (v.id.equals(name))
                throw new SemanticException("Duplicate parameter '" + name + "' in '" + method.id + "' method declaration");
        }
        method.parameters.add(param);
        symbolTable.addLocal(name, param);

        return type + " " + name;
    }


    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this, null);
    }

    public String visit(BooleanArrayType n, String argu) {
        return "boolean[]";
    }

    public String visit(IntegerArrayType n, String argu) {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}