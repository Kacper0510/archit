package archit.common;

import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

public class ArchitVisitor extends ArchitBaseVisitor<Void> {

    private final Interpreter interpreter;
    private final ScriptRun run;

    ArchitVisitor(Interpreter interpreter, ScriptRun run) {
        this.interpreter = interpreter;
        this.run = run;
    }

    @Override
    public Void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {

        String funcName = ctx.ID().getText();

        switch (funcName) {
            case "print" -> {
                var expr0 = ctx.expr(0);
                String message = expr0.getText();
                interpreter.builtinPrint(run, message);
            }
            case "move" -> {
            int x = Integer.parseInt(ctx.expr(0).getText());
            int y = Integer.parseInt(ctx.expr(1).getText());
            int z = Integer.parseInt(ctx.expr(2).getText());
            interpreter.builtinMove(run, x, y, z);
            }
            case "place" -> {
            String material = ctx.expr(0).getText();
            interpreter.builtinPlace(run, material);
            }
            default -> interpreter.getLogger().scriptError(run, "Nieznana funkcja: {}", funcName);
        }

        return super.visitChildren(ctx);
    }

    @Override
    public Void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {

        String funcName = ctx.ID().getText();

        switch (funcName) {
            case "print" -> {
                if (!ctx.expr().isEmpty()) {
                    String message = ctx.expr(0).getText();
                    interpreter.builtinPrint(run, message);
                }
            }
            case "move" -> {
                if (ctx.expr().size() >= 3) {
                    int x = Integer.parseInt(ctx.expr(0).getText());
                    int y = Integer.parseInt(ctx.expr(1).getText());
                    int z = Integer.parseInt(ctx.expr(2).getText());
                    interpreter.builtinMove(run, x, y, z);
                } else {
                    interpreter.getLogger().scriptError(run, "move wymaga 3 argumentów");
                }
            }
            case "place" -> {
                if (!ctx.expr().isEmpty()) {
                    String material = ctx.expr(0).getText();
                    interpreter.builtinPlace(run, material);
                }
            }
            default -> interpreter.getLogger().scriptError(run, "Nieznana funkcja: {}", funcName);
        }

        return super.visitChildren(ctx);
    }
}