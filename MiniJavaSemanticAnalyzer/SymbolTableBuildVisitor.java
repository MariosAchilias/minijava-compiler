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
        System.out.println("Class: " + classname);

        super.visit(n, argu);

        System.out.println();

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
        System.out.println("calld class declaration");
        n.f0.accept(this, null);

        String className = n.f1.accept(this, null);

        Class classSymbol = new Class(className);

        n.f2.accept(this, null);
        n.f3.accept(this, classSymbol);
        n.f4.accept(this, classSymbol);
        n.f5.accept(this, null);

        symbolTable.addSymbol(className, classSymbol);

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
        n.f0.accept(this, argu);

        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        System.out.println("Fields: ");
        n.f5.accept(this, argu);
        System.out.println("Methods: ");
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);

        System.out.println();

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, Symbol argu) throws Exception {
        VarType type = VarType.getType(n.f0.accept(this, argu));
        String name = n.f1.accept(this, argu);
        Variable var = new Variable(type, name);
        symbolTable.addSymbol(name, var);
        if (argu.type == SymbolType.CLASS) {
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
        VarType retType = VarType.getType(n.f1.accept(this, null));
        String name = n.f2.accept(this, null);

        Method method = new Method(retType, name, null);
        // FormalParameterList visitor should create and set array
        if (n.f4.present())
            n.f4.accept(this, method);

        assert(argu != null && argu.type == SymbolType.CLASS);
        Class class_ = (Class) argu;
        class_.addMethod(method);
        // TODO statement local vars scope etc

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
    public String visit(FormalParameter n, Symbol argu) throws Exception{
        assert(argu.type != null && argu.type == SymbolType.METHOD);

        VarType type = VarType.getType(n.f0.accept(this, null));
        String name = n.f1.accept(this, null);

        Method method = (Method) argu;
        method.parameters.add(new Variable(type, name));

        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, Symbol argu) {
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