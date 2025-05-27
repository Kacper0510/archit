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
            throw new ScriptException(
                run, LOGIC_ERROR, ctx, "Some branches do not return in function {}", ctx.ID().getText()
            );
        }
    }

    public static void checkLoop(ArchitParser.WhileStatContext ctx, ScriptRun run) {
        new DeadCodeVisitor(false, run).visitScopeStat(ctx.scopeStat());
    }

    public static void checkLoop(ArchitParser.RepeatStatContext ctx, ScriptRun run) {
        new DeadCodeVisitor(false, run).visitScopeStat(ctx.scopeStat());
    }
}
