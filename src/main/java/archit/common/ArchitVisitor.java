package archit.common;

import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;
import java.util.List;

public class ArchitVisitor extends ArchitBaseVisitor<Void> {

    private final Interpreter interpreter;
    private final ScriptRun run;

    ArchitVisitor(Interpreter interpreter, ScriptRun run) {
        this.interpreter = interpreter;
        this.run = run;
    }

    @Override
    public Void visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        int iterations = Integer.parseInt(ctx.expr().NUMBER().getText());
        for (int i = 0; i < iterations; i++) {
            super.visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText();
        functionCall(funcName, ctx.expr());
        return super.visitChildren(ctx);
    }

    @Override
    public Void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        String funcName = ctx.ID().getText();
        functionCall(funcName, ctx.expr());
        return super.visitChildren(ctx);
    }

    private void functionCall(String functionName, List<ArchitParser.ExprContext> params) {
        switch (functionName) {
            case "print" -> {
                if (!params.isEmpty()) {
                    String message = params.get(0).getText();
                    if (message.length() >= 2) {
                        char first = message.charAt(0);
                        char last = message.charAt(message.length() - 1);
                        if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                            message = message.substring(1, message.length() - 1);
                        }
                    }
                    interpreter.builtinPrint(run, message);
                }
            }
            case "move" -> {
                if (params.size() == 3) {
                    int y = Integer.parseInt(params.get(1).getText());
                    int x = Integer.parseInt(params.get(0).getText());
                    int z = Integer.parseInt(params.get(2).getText());
                    interpreter.builtinMove(run, x, y, z);
                } else if (params.size() == 1) {
                    String direction = params.get(0).enumExpr().ID().getText();
                    switch(direction) {
                        case "posx" -> interpreter.builtinMove(run, 1, 0, 0);
                        case "posy" -> interpreter.builtinMove(run, 0, 1, 0);
                        case "posz" -> interpreter.builtinMove(run, 0, 0, 1);
                        case "negx" -> interpreter.builtinMove(run, -1, 0, 0);
                        case "negy" -> interpreter.builtinMove(run, 0, -1, 0);
                        case "negz" -> interpreter.builtinMove(run, 0, 0, -1);
                        default -> interpreter.getLogger().scriptError(run, "Unknown direction: {}", direction);
                    }

                } else {
                    interpreter.getLogger().scriptError(run, "move requires one or three arguments");
                }
            }
            case "place" -> {
                if (!params.isEmpty()) {
                    String material = params.get(0).getText();
                    if (material.startsWith(":")) {
                        material = "minecraft" + material;
                    }
                    interpreter.builtinPlace(run, material);
                }
            }
            default -> interpreter.getLogger().scriptError(run, "Unknown function: {}", functionName);
        }
    }
}
