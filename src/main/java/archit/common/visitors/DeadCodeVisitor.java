package archit.common.visitors;

import archit.common.ScriptException;
import archit.common.ScriptRun;
import archit.parser.ArchitParser;
import archit.parser.ArchitParserBaseVisitor;

import static archit.common.ScriptException.Type.DEAD_CODE;
import static archit.common.ScriptException.Type.LOGIC_ERROR;

public class DeadCodeVisitor extends ArchitParserBaseVisitor<Boolean> {
    private final boolean returnMode;
    private final ScriptRun run;

    private DeadCodeVisitor(boolean returnMode, ScriptRun run) {
        this.returnMode = returnMode;
        this.run = run;
    }

    public static void checkFunction(ArchitParser.FunctionDeclContext ctx, boolean voidFunction, ScriptRun run) {
        var visitor = new DeadCodeVisitor(true, run);
        var allReturn = visitor.visitScopeStat(ctx.scopeStat());
        if (!allReturn && !voidFunction) {
            var location = ctx.scopeStat().statement().isEmpty() ? ctx : ctx.scopeStat().statement().getLast();
            throw new ScriptException(
                run, LOGIC_ERROR, location, "Some branches do not return in function '{}'", ctx.ID().getText()
            );
        }
    }

    public static void checkLoop(ArchitParser.WhileStatContext ctx, ScriptRun run) {
        new DeadCodeVisitor(false, run).visitScopeStat(ctx.scopeStat());
    }

    public static void checkLoop(ArchitParser.RepeatStatContext ctx, ScriptRun run) {
        new DeadCodeVisitor(false, run).visitScopeStat(ctx.scopeStat());
    }

    @Override
    public Boolean visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        boolean foundStop = false;
        for (var stat : ctx.statement()) {
            if (foundStop) {
                throw new ScriptException(run, DEAD_CODE, stat, "Unreachable statement");
            }
            if (visit(stat)) {
                foundStop = true;
            }
        }
        return foundStop;
    }

    @Override
    public Boolean visitIfStat(ArchitParser.IfStatContext ctx) {
        if (ctx.elseStat() == null) {
            return false;  // non-exhaustive
        }
        return visit(ctx.scopeStat()) && visit(ctx.elseStat());
    }

    @Override
    public Boolean visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        return true;
    }

    @Override
    public Boolean visitBreakStat(ArchitParser.BreakStatContext ctx) {
        return !returnMode;
    }

    @Override
    public Boolean visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        return !returnMode;
    }

    @Override
    public Boolean visitWhileStat(ArchitParser.WhileStatContext ctx) {
        return false;  // loop sometimes does not iterate at all
    }

    @Override
    public Boolean visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        return false;  // loop sometimes does not iterate at all
    }

    @Override
    public Boolean visitFunctionDecl(ArchitParser.FunctionDeclContext ctx) {
        return false;
    }

    @Override
    protected Boolean defaultResult() {
        return false;
    }
}
