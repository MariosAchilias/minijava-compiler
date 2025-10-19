import Emitter.*;
import Parser.syntaxtree.*;
import Parser.visitor.*;
import SymbolTable.*;

class EmitIRVisitor extends GJDepthFirst<String, String>{
    SymbolTable symbolTable;
    Emitter emitter;
    public EmitIRVisitor(SymbolTable symbolTable, Emitter emitter) {
        this.symbolTable = symbolTable;
        this.emitter = emitter;
    }

    @Override
    public String visit(Goal n, String argu) throws Exception {
        emitter.emitVTables();
        emitter.emitHelpers();
        n.f0.accept(this, null);
        n.f1.accept(this, null);
        return null;
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
        Method m = new Method ("void", "main", null, null);
        emitter.emitMethodStart(m);
        n.f15.accept(this, null);
        emitter.emitMethodEnd(m, "");
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
        n.f4.accept(this, className);
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
        n.f4.accept(this, className);
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
        // Evil ugly hack -- passing null to Identifier visitor will make it return name
        // otherwise, returns a register with lvalue address
        // this is ugly, but no obvious solution comes to me
        // TODO: refactor 

        String type = symbolTable.getVar(n.f0.accept(this, null)).varType;
        
        String lhs = n.f0.accept(this, "true");
        String rhs = n.f2.accept(this, "true");
        // lhs is register that contains memory location of lvalue
        // rhs is register that contains value
        emitter.emitAssignment(type, lhs, rhs);

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
        String methodName = n.f2.accept(this, null);
        Method m = symbolTable.getMethod(methodName, className);
        emitter.emitMethodStart(m);
        symbolTable.enterScope(m.getLocalScope());

        n.f8.accept(this, null);

        emitter.emitMethodEnd(m, n.f10.accept(this, null));

        return null;
    }

    @Override
    public String visit(PrintStatement n, String argu) throws Exception {
        emitter.emitPrintInt(n.f2.accept(this, null));

        return null;
    }

    // Non-trivial leaf nodes

    @Override
    public String visit(Identifier n, String returnReg) {
        String id = n.f0.toString();
        if (returnReg == null)
            return id;

        // Evil hack (TODO: maybe refactor)
        // if null is passed as argument, return identifier text
        // otherwise we use this for lvalue assignment

        // Used for lvalues, returns register that contains a pointer to the value
        return emitter.emitLvalueAddressOf(id);
    }

    @Override
    public String visit(ThisExpression n, String argu) throws Exception {
        return null; // TODO
    }

    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return null; // TODO
    }

    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        return null; // TODO
    }

    // Expressions
    
    @Override
    public String visit(PlusExpression n, String argu) throws Exception {
        String first = n.f0.accept(this, "returnReg");
        String second = n.f2.accept(this, "returnReg");
        return emitter.emitBinaryOperation("add", first, second);
    }

    @Override
    public String visit(MinusExpression n, String argu) throws Exception {
        String first = n.f0.accept(this, "returnReg");
        String second = n.f2.accept(this, "returnReg");
        return emitter.emitBinaryOperation("add", first, second);
    }

    @Override
    public String visit(TimesExpression n, String argu) throws Exception {
        String first = n.f0.accept(this, null);
        String second = n.f2.accept(this, null);
        return emitter.emitBinaryOperation("mul", first, second);
    }

    @Override
    public String visit(AndExpression n, String argu) throws Exception {
        String first = n.f0.accept(this, null);
        String second = n.f2.accept(this, null);
        return emitter.emitBinaryOperation("and", first, second);
    }

    @Override
    public String visit(CompareExpression n, String argu) throws Exception {
        String first = n.f0.accept(this, null);
        String second = n.f2.accept(this, null);
        return emitter.emitBinaryOperation("slt", first, second);
    }

    // Array related methods

    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {
        // TODO
        return null;
    }

    @Override
    public String visit(ArrayLength n, String argu) throws Exception {
        // TODO
        // this one needs some thinking, maybe store array lengths somewhere (will also be used for bounds checking)
        return null;
    }

    // Trivial methods

    @Override
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, "returnReg");
    }

    @Override
    public String visit(Expression n, String argu) throws Exception {
        // Used for rvalues, returns register that contains the result of the expression evaluation
        return n.f0.accept(this, null);
    }

    @Override
    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this, null);
    }

    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return n.f0.toString();
    }

    @Override
    public String visit(TrueLiteral n, String argu) throws Exception {
        return n.f0.toString();
    }

    @Override
    public String visit(FalseLiteral n, String argu) throws Exception {
        return n.f0.toString();
    }

    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, null);
    }

}