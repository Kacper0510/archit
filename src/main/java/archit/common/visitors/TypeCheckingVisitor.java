package archit.common.visitors;

import archit.common.Scope;
import archit.common.ScriptRun;
import archit.common.Type;
import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

public class TypeCheckingVisitor extends ArchitBaseVisitor<Type> {
    private final ScriptRun run;
    private final InfoTables tables = new InfoTables();
    private Scope currentScope;

    public TypeCheckingVisitor(ScriptRun run) {
        this.run = run;
        this.currentScope = run.getInterpreter().getStandardLibrary();
        pushScope();  // push global scope
    }

    private void pushScope() {
        currentScope = new ScopeImpl(currentScope);
    }

    private void popScope() {
        currentScope = currentScope.getParent();
    }

    public InfoTables getTables() {
        return tables;
    }

    @Override
    public Type visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStat'");
    }

    @Override
    public Type visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        pushScope();
        visitChildren(ctx);
        popScope();
        return null;
    }

    @Override
    public Type visitVarDecl(ArchitParser.VarDeclContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitVarDecl'");
    }

    @Override
    public Type visitAssignStat(ArchitParser.AssignStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignStat'");
    }

    @Override
    public Type visitType(ArchitParser.TypeContext ctx) {
        return Type.fromTypeContext(ctx);
    }

    @Override
    public Type visitSymbol(ArchitParser.SymbolContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitSymbol'");
    }

    @Override
    public Type visitIfStat(ArchitParser.IfStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStat'");
    }

    @Override
    public Type visitElseIfStat(ArchitParser.ElseIfStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitElseIfStat'");
    }

    @Override
    public Type visitElseStat(ArchitParser.ElseStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitElseStat'");
    }

    @Override
    public Type visitExpr(ArchitParser.ExprContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitExpr'");
    }

    @Override
    public Type visitListExpr(ArchitParser.ListExprContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitListExpr'");
    }

    @Override
    public Type visitMapExpr(ArchitParser.MapExprContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitMapExpr'");
    }

    @Override
    public Type visitEnumExpr(ArchitParser.EnumExprContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitEnumExpr'");
    }

    @Override
    public Type visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitMaterialExpr'");
    }

    @Override
    public Type visitWhileStat(ArchitParser.WhileStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitWhileStat'");
    }

    @Override
    public Type visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitRepeatStat'");
    }

    @Override
    public Type visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionCall'");
    }

    @Override
    public Type visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionCallNoBrackets'");
    }

    @Override
    public Type visitFunctionDecl(ArchitParser.FunctionDeclContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionDecl'");
    }

    @Override
    public Type visitFunctionParam(ArchitParser.FunctionParamContext ctx) {
        // TODO (dawid)
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionParam'");
    }
}
