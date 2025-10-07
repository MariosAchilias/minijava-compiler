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
        emitter.emitMethodStart(new Method ("int", "main", null, null));

        emitter.emitMethodEnd();
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
        // TODO return expression

        emitter.emitMethodEnd();

        return null;
    }

    // Leaf nodes

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

    // Trivial visitors

    @Override
    public String visit(PrimaryExpression n, String returnReg) throws Exception {
        return n.f0.accept(this, returnReg);
    }

    /* f0 -> AndExpression()
 *       | CompareExpression()
 *       | PlusExpression()
 *       | MinusExpression()
 *       | TimesExpression()
 *       | ArrayLookup()
 *       | ArrayLength()
 *       | MessageSend()
 *       | Clause()
*/
    @Override
    public String visit(Expression n, String returnReg) throws Exception {
        // Used for rvalues, returns register that contains the result of the expression evaluation
        return n.f0.accept(this, returnReg);
    }

    @Override
    public String visit(Clause n, String returnReg) throws Exception {
        return n.f0.accept(this, returnReg);
    }


}