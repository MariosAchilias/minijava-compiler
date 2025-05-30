import syntaxtree.*;
import visitor.*;

import java.util.ArrayList;

class TypeCheckVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    public TypeCheckVisitor() {
        symbolTable = SymbolTable.getInstance();
    }

    public String visit(MainClass n, String argu) throws Exception {
        symbolTable.enterScope(symbolTable.getMainScope());
        /* f15 -> ( Statement() ) */
        n.f15.accept(this, argu);
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
        String className = n.f1.accept(this, argu);
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
        String className = n.f1.accept(this, argu);
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

        String returnType = n.f1.accept(this, className);
        String methodName = n.f2.accept(this, className);
        Method method = symbolTable.getMethod(methodName, className);

        assert method != null;
        symbolTable.enterScope(method.getLocalScope());
        n.f8.accept(this, className);


        String returnExprType = n.f10.accept(this, className);
        if (!returnExprType.equals(returnType))
            throw new SemanticException("Return type " + returnExprType + " of method " + className + "." + methodName + " doesn't match declared type " + returnType);

        symbolTable.exitLocalScope();

//        super.visit(n, argu);
        return null;
    }

    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    public String visit(NotExpression n, String argu) throws Exception {
        if(!"boolean".equals(n.f1.accept(this, argu)))
            throw new SemanticException("Not operator used on non-boolean expression");
        return "boolean";
    }

    public String visit(AndExpression n, String argu) throws Exception {
        if (!"boolean".equals(n.f0.accept(this, argu)) || !"boolean".equals(n.f2.accept(this, argu)))
            throw new SemanticException("Operand of && operator is non-boolean");
        return "boolean";
    }

    public String visit(CompareExpression n, String argu) throws Exception {
        String leftOpType = n.f0.accept(this, argu);
        String rightOpType = n.f2.accept(this, argu);
        if (!leftOpType.equals("int") || !rightOpType.equals("int"))
            throw new SemanticException("\"<\" operator given non-integer argument");
        return "boolean";
    }

    public String visit(PlusExpression n, String argu) throws Exception {
        String leftOpType = n.f0.accept(this, argu);
        String rightOpType = n.f2.accept(this, argu);
        if (!leftOpType.equals("int") || !rightOpType.equals("int"))
            throw new SemanticException("\"+\" operator given non-integer argument");
        return "int";
    }

    public String visit(TimesExpression n, String argu) throws Exception {
        String leftOpType = n.f0.accept(this, argu);
        String rightOpType = n.f2.accept(this, argu);
        if (!leftOpType.equals("int") || !rightOpType.equals("int"))
            throw new SemanticException("\"*\" operator given non-integer argument");
        return "int";
    }

    public String visit(ArrayLookup n, String argu) throws Exception {
        if (!n.f2.accept(this, argu).equals("int"))
            throw new SemanticException("Non-integer value used as array index");

        String arrayType = n.f0.accept(this, argu);
        if (arrayType.equals("int[]"))
            return "int";
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
            return n.f0.accept(this, argu);

        String id = n.f0.accept(this, argu);
        Variable var = symbolTable.getLocal(id);
        if (var == null)
            throw new SemanticException("Identifier " + id + " not found in scope");
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

    public String visit(ThisExpression n, String className) throws Exception {
        return className;
    }

    /**
     * Grammar production:
     * f0 -> BooleanArrayAllocationExpression()
     *       | IntegerArrayAllocationExpression()
     */
    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
         /* f3 -> Expression() */
        if(!"int".equals(n.f3.accept(this, argu)))
            throw new SemanticException("Array size in allocation expression must evaluate to an integer");
        return "boolean[]";
    }

    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        if(!"int".equals(n.f3.accept(this, argu)))
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
        String id = n.f1.accept(this, argu);
        if (symbolTable.getClass(id) == null) {
            throw new SemanticException("Instantiation of undefined class '" + id + "'");
        }
        return id;
    }

    /**
     * Grammar production:
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
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
        Variable leftHandSide = symbolTable.getLocal(id);
        if(leftHandSide == null)
            throw new SemanticException("Assignment to undefined variable " + id);

        String exprType = n.f2.accept(this, argu);
        if(!leftHandSide.varType.equals(exprType))
            throw new Exception("Assignment expression type doesn't match left hand side variable type");
        return null;
    }

    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        if (!"int".equals(n.f2.accept(this, argu)))
            throw new SemanticException("Array assignment with non-integer expression as index");

        String arrayType = symbolTable.getLocal(n.f0.accept(this, argu)).varType;
        String exprType = symbolTable.getLocal(n.f4.accept(this, argu)).varType;

        if (arrayType.equals("int[]") && exprType.equals("int"))
            return null;

        if (arrayType.equals("boolean[]") && exprType.equals("boolean"))
            return null;

        throw new SemanticException("Array assignment to value of incompatible type");
    }

    /**
    * Grammar production:
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    @Override
    public String visit(IfStatement n, String argu) throws Exception {
        if(!"boolean".equals(n.f2.accept(this, argu)))
            throw new SemanticException("Non-boolean expression in if statement condition");

        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
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
        // TODO: method calls
        String className = n.f0.accept(this, argu);
        if (symbolTable.getClass(className) == null)
            throw new SemanticException("Method call to method of undefined class");

        String methodName = n.f2.accept(this, argu);
        Method method = symbolTable.getMethod(methodName, className);
        if (method == null)
            throw new SemanticException("Class " + className + " or its parent classes have no method " + methodName);

        java.util.ArrayList<Variable> tempParameters = new ArrayList<>();

        if (method.parameters.isEmpty() && !n.f4.present()) // no parameters given to void method -- correct
            return method.returnType;

        String argTypes = n.f4.accept(this, argu); // semicolon-separated types
        for (String s: argTypes.split(";"))
            tempParameters.add(new Variable(s, ""));
        if(!Method.compatibleParameters(method.parameters, tempParameters))
            throw new SemanticException("Method call doesn't match method parameters in number or types");

        return method.returnType;
    }

    public String visit(ExpressionList n, String argu) throws Exception {
        return n.f0.accept(this, argu) + ";" + n.f1.accept(this, argu);
    }

    public String visit(ExpressionTail n, String argu) throws Exception {
        if (!n.f0.present())
            return "";
        return n.f0.accept(this, argu);
    }

    @Override
    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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