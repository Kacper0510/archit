package archit.common.visitors;

import archit.common.ScriptRun;
import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

public class EvaluationVisitor extends ArchitBaseVisitor<Object> {
    private final ScriptRun run;

    public EvaluationVisitor(ScriptRun run) {
        this.run = run;
    }

    @Override
    public Object visitAssignStat(ArchitParser.AssignStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssignStat(ctx);
    }

    @Override
    public Object visitBreakStat(ArchitParser.BreakStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitBreakStat(ctx);
    }

    @Override
    public Object visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitContinueStat(ctx);
    }

    @Override
    public Object visitElseIfStat(ArchitParser.ElseIfStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitElseIfStat(ctx);
    }

    @Override
    public Object visitElseStat(ArchitParser.ElseStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitElseStat(ctx);
    }

    @Override
    public Object visitEnumExpr(ArchitParser.EnumExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitEnumExpr(ctx);
    }

    @Override
    public Object visitExpr(ArchitParser.ExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExpr(ctx);
    }

    @Override
    public Object visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFunctionCall(ctx);
    }

    @Override
    public Object visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFunctionCallNoBrackets(ctx);
    }

    @Override
    public Object visitIfStat(ArchitParser.IfStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitIfStat(ctx);
    }

    @Override
    public Object visitListExpr(ArchitParser.ListExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitListExpr(ctx);
    }

    @Override
    public Object visitMapExpr(ArchitParser.MapExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitMapExpr(ctx);
    }

    @Override
    public Object visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitMaterialExpr(ctx);
    }

    @Override
    public Object visitProgram(ArchitParser.ProgramContext ctx) {
        // TODO Auto-generated method stub
        return super.visitProgram(ctx);
    }

    @Override
    public Object visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitRepeatStat(ctx);
    }

    @Override
    public Object visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitReturnStat(ctx);
    }

    @Override
    public Object visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitScopeStat(ctx);
    }

    @Override
    public Object visitVarDecl(ArchitParser.VarDeclContext ctx) {
        // TODO Auto-generated method stub
        return super.visitVarDecl(ctx);
    }

    @Override
    public Object visitWhileStat(ArchitParser.WhileStatContext ctx) {
        // TODO Auto-generated method stub
        return super.visitWhileStat(ctx);
    }
}
