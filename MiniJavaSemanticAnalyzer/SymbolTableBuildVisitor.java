import syntaxtree.*;
import visitor.*;

class SymbolTableBuildVisitor extends GJDepthFirst<String, Symbol>{
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
    public String visit(MainClass n, Symbol argu) throws Exception {
        String classname = n.f1.accept(this, null);
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
    public String visit(ClassDeclaration n, Symbol argu) throws Exception {
        n.f0.accept(this, null);

        String className = n.f1.accept(this, null);

        Class classSymbol = new Class(className, null);
        symbolTable.addClass(className, classSymbol);
        symbolTable.enterScope(classSymbol.getScope());

        n.f2.accept(this, null);
        n.f3.accept(this, classSymbol);
        n.f4.accept(this, classSymbol);
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
    public String visit(ClassExtendsDeclaration n, Symbol argu) throws Exception {
        n.f0.accept(this, null);

        String className = n.f1.accept(this, null);
        String superClassName = n.f3.accept(this, null);

        // check error (superclass not defined) TODO
        Class superClass = symbolTable.getClass(superClassName);

        Class classSymbol = new Class(className, superClass);

        symbolTable.addClass(className, classSymbol);

        symbolTable.enterScope(classSymbol.getScope());

        n.f5.accept(this, classSymbol);
        n.f6.accept(this, classSymbol);

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, Symbol argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);

        if (symbolTable.getLocal(name) != null)
            throw new SemanticException("Double definition of variable " + name);

        Variable var = new Variable(type, name);
        System.out.println("Declared variable: " + type + " " + name);
        symbolTable.addLocal(name, var);
        if (argu != null && argu.type == SymbolType.CLASS) {
            Class class_ = (Class) argu;
            class_.addField(var);
        }
//        super.visit(n, argu);
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
    public String visit(MethodDeclaration n, Symbol argu) throws Exception {
//        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        assert(argu != null && argu.type == SymbolType.CLASS);
        Class class_ = (Class) argu;

        String retType = n.f1.accept(this, null);
        String name = n.f2.accept(this, null);

        Method method = new Method(retType, name, null, class_.id);

        symbolTable.addMethod(name, class_.id, method);

        symbolTable.enterScope(method.getLocalScope());

        // FormalParameterList visitor should create and set array
        if (n.f4.present())
            n.f4.accept(this, method);


        n.f7.accept(this, null);

        symbolTable.exitLocalScope();

        // If method exists in parent class, it must be a valid override (i.e. have same return & argument types)
        if (class_.getParent() == null || symbolTable.getMethod(name, class_.getParent().id) == null)
            return null;

        Method overriden = symbolTable.getMethod(name, class_.getParent().id);
        if (!Method.compatibleSignatures(method, overriden))
            throw new SemanticException("Method override has incompatible return type and/or argument types");

//        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Symbol argu) throws Exception {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null) {
            ret += n.f1.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, Symbol argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Symbol argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Symbol argu) throws Exception {
        assert(argu.type != null && argu.type == SymbolType.METHOD);

        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        Method method = (Method) argu;
        Variable param = new Variable(type, name);
        method.parameters.add(param);
        symbolTable.addLocal(name, param);

        return type + " " + name;
    }


    public String visit(ArrayType n, Symbol argu) throws Exception {
        return n.f0.accept(this, null);
    }

    public String visit(BooleanArrayType n, Symbol argu) {
        return "boolean[]";
    }

    public String visit(IntegerArrayType n, Symbol argu) {
        return "int[]";
    }

    public String visit(BooleanType n, Symbol argu) {
        return "boolean";
    }

    public String visit(IntegerType n, Symbol argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Symbol argu) {
        return n.f0.toString();
    }
}