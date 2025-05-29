import syntaxtree.*;
import visitor.*;

class TypeCheckVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    public TypeCheckVisitor() {
        symbolTable = SymbolTable.getInstance();
    }

    public String visit(MainClass n, String argu) throws Exception {
        symbolTable.enterScope(symbolTable.getMainScope());
        /* f15 -> ( Statement() ) */
        n.f15.accept(this, null);
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
        String className = n.f1.accept(this, null);
        Class classSymbol = symbolTable.getClass(className);

        symbolTable.enterScope(classSymbol.getScope());
        n.f4.accept(this, className);
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
        String className = n.f1.accept(this, null);
        Class classSymbol = symbolTable.getClass(className);

        symbolTable.enterScope(classSymbol.getScope());
        n.f6.accept(this, className);
        symbolTable.exitLocalScope();

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

        String returnType = n.f1.accept(this, null);
        String methodName = n.f2.accept(this, null);
        Method method = symbolTable.getMethod(methodName, className);

        assert method != null;
        symbolTable.enterScope(method.getLocalScope());
        n.f8.accept(this, null);


        // TODO check that return expression type matches method return type

        String returnExprType = n.f10.accept(this, null);
        if (!returnExprType.equals(returnType))
            throw new SemanticException("Return type " + returnExprType + " of method " + className + "." + methodName + " doesn't match declared type " + returnType);

        symbolTable.exitLocalScope();

//        super.visit(n, argu);
        return null;
    }

    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this, null);
    }

    public String visit(NotExpression n, String argu) throws Exception {
        if(!"boolean".equals(n.f1.accept(this, null)))
            throw new SemanticException("Not operator used on non-boolean expression");
        return "boolean";
    }

    public String visit(AndExpression n, String argu) throws Exception {
        if (!"boolean".equals(n.f0.accept(this, argu)) || !"boolean".equals(n.f2.accept(this, argu)))
            throw new SemanticException("Operand of && operator is non-boolean");
        return "boolean";
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
    public String visit(PrimaryExpression n, String argu) throws Exception {
        if (n.f0.which != 3)
            return n.f0.accept(this, null);

        String id = n.f0.accept(this, null);
        return symbolTable.getLocal(id).varType;
    }

    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    public String visit(TrueLiteral n, String argu) throws Exception {
        return "boolean";
    }

    public String visit(FalseLiteral n, String argu) throws Exception {
        return "boolean";
    }

    public String visit(ThisExpression n, String argu) throws Exception {
        return "ThisExpression";
    }

    /**
     * Grammar production:
     * f0 -> BooleanArrayAllocationExpression()
     *       | IntegerArrayAllocationExpression()
     */
    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return n.f0.accept(this, null);
    }

    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
         /* f3 -> Expression() */
        if(!"int".equals(n.f3.accept(this, null)))
            throw new SemanticException("Array size in allocation expression must evaluate to an integer");
        return "boolean[]";
    }

    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        if(!"int".equals(n.f3.accept(this, null)))
            throw new SemanticException("Array size in allocation expression must evaluate to an integer");
        return "int[]";
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
        if (symbolTable.getClass(id) == null) {
            throw new SemanticException("Instantiation of undefined class \'" + id + "\'");
        }
        return "AllocationExpression";
    }

    /**
     * Grammar production:
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, null);
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
        if(symbolTable.getLocal(id) == null)
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