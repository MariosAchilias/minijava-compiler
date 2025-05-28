import syntaxtree.*;
import visitor.*;

class TypeCheckVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    public TypeCheckVisitor() {
        symbolTable = SymbolTable.getInstance();
    }

    // TODO main class

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
        String className = n.f1.accept(this, null);
        Class classSymbol = (Class) symbolTable.getSymbol(className);

        symbolTable.enterScope(classSymbol.getScope());
        n.f4.accept(this, null);
        symbolTable.enterGlobalScope();

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
        String className = n.f1.accept(this, null);
        Class classSymbol = (Class) symbolTable.getSymbol(className);

        symbolTable.enterScope(classSymbol.getScope());
        n.f6.accept(this, null);
        symbolTable.enterGlobalScope();

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
    public String visit(MethodDeclaration n, String argu) throws Exception {

        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);
        Method method = (Method) symbolTable.getSymbol(methodName);

        symbolTable.enterScope(method.getLocalScope());
        n.f8.accept(this, null);
        symbolTable.exitScope();

        // TODO check that return expression type matches method return type

//        super.visit(n, argu);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, argu);
        if(symbolTable.getSymbol(id) == null)
            throw new SemanticException("Assignment to undefined variable " + id);

        // TODO check type of expression (two possible ways: return type from expression, or give expected type as argument to expression visitor)
        n.f2.accept(this, argu);
        return null;
    }
    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, String argu) throws Exception {
        String primaryExpression = n.f0.accept(this, null);

        String id = n.f2.accept(this, null);
        n.f4.accept(this, argu);
        return null;
    }

    /**
     * Grammar production:
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception {
        n.f0.accept(this, argu);
        return null;
    }


    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        String id = n.f1.accept(this, null);
        Symbol classSymbol = symbolTable.getSymbol(id);
        if (classSymbol == null) {
            throw new SemanticException("Instantiation of undefined class \'" + id + "\'");
        }
        return null;
    }

    @Override
    public String visit(ArrayType n, String argu) {
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