import syntaxtree.*;
import visitor.*;
import SymbolTable.*;
import SymbolTable.Class;

class SymbolTableBuildVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    public SymbolTableBuildVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
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
        symbolTable.main = new Class(n.f1.accept(this, null), null);
        symbolTable.enterClassScope(symbolTable.main);
        n.f14.accept(this, null);
        symbolTable.exitClassScope();

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

        Class newClass = symbolTable.newClass(className, null);
        symbolTable.enterClassScope(newClass);

        n.f2.accept(this, null);
        n.f3.accept(this, className);
        n.f4.accept(this, className);
        n.f5.accept(this, null);

        symbolTable.exitClassScope();

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

        Class newClass = symbolTable.newClass(className, superClass);

        symbolTable.enterClassScope(newClass);

        n.f5.accept(this, className);
        n.f6.accept(this, className);

        symbolTable.exitClassScope();

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

        if (symbolTable.getLocalVar(name) != null)
            throw new SemanticException("Double definition of variable " + name);

        Variable var = new Variable(type, name);
        symbolTable.addVariable(name, var);
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
        Class class_ = symbolTable.getClass(className);
        if (class_.getLocalMethod(name) != null)
            throw new SemanticException("Double declaration of method '"+ name + "'");

        String retType = n.f1.accept(this, null);
        Method method = new Method(retType, name, null, class_.getVariableScope());

        class_.addMethod(method);

        symbolTable.enterScope(method.getLocalScope());

        // FormalParameterList visitor should create and set array
        if (n.f4.present())
            n.f4.accept(this, className + ";" + name);

        n.f7.accept(this, null);

        symbolTable.exitLocalScope();

        Class parent = symbolTable.getClass(className).getParent();
        Method toOverride = class_.getMethod(name);
        if (toOverride == null)
            return null;

        if (!method.returnType.equals(toOverride.returnType))
            throw new SemanticException("Method override has different return type");
        if (!symbolTable.compatibleMethodParameters(method.parameters, toOverride.parameters))
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
        symbolTable.addVariable(name, param);

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