package archit.common;

import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

import java.util.List;


public class ArchitVisitor extends ArchitBaseVisitor<Void> {

    private final Interpreter interpreter;
    private final ScriptRun run;

    VariableTable variableTable;


    ArchitVisitor(Interpreter interpreter, ScriptRun run, VariableTable variableTable) {
        this.interpreter = interpreter;
        this.run = run;
        this.variableTable = variableTable;
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


    // evaluating
    private Value evaluate(ArchitParser.ExprContext ctx) {
        if (ctx.NUMBER() != null) {
            return new Value(Integer.parseInt(ctx.NUMBER().getText()), "number");
        }
        if (ctx.REAL() != null) {
            return new Value(Double.parseDouble(ctx.REAL().getText()), "real");
        }
        if (ctx.LOGIC() != null) {
            return new Value(Boolean.parseBoolean(ctx.LOGIC().getText()), "logic");
        }
        if (ctx.STRING() != null) {
            String text = ctx.STRING().getText();
            text = text.substring(1, text.length() - 1); // deleting cudzuslowy xd no quotation marks
            // interpolation
            text = interpolateString(text);
            return new Value(text, "string");
        }
        if (ctx.ID() != null) {
            String name = ctx.ID().getText();
            if (!variableTable.isDeclared(name)) {
                throw new VariableTable.VariableException("Unknown variable: " + name);
            }
            return variableTable.getValue(name);
        }
        if (ctx.BINARY_OP() != null) {
            Value left = evaluate(ctx.expr(0));
            Value right = evaluate(ctx.expr(1));
            return evalBinaryOp(left, ctx.BINARY_OP().getText(), right);
        }

        // TODO: function for expressions
        throw new RuntimeException("Unsupported expression: " + ctx.getText());
    }

    // The real func for string interpolation
    private String interpolateString(String text) {
        // TODO
        return text;
    }

    // binary .//'
    private Value evalBinaryOp(Value left, String op, Value right) {
        if (left.type.equals("number") && right.type.equals("number")) {
            int l = (Integer) left.value;
            int r = (Integer) right.value;
            return switch (op) {
                case "+" -> new Value(l + r, "number");
                case "-" -> new Value(l - r, "number");
                case "*" -> new Value(l * r, "number");
                case "/" -> new Value(l / r, "number");
                case "%" -> new Value(l % r, "number");
                case "==" -> new Value(l == r, "logic");
                case "!=" -> new Value(l != r, "logic");
                case ">" -> new Value(l > r, "logic");
                case "<" -> new Value(l < r, "logic");
                case ">=" -> new Value(l >= r, "logic");
                case "<=" -> new Value(l <= r, "logic");
                case "^" -> new Value((int) Math.pow(l, r), "number");
                default -> throw new RuntimeException("Unknown operator: " + op);
            };
        }
        if (left.type.equals("logic") && right.type.equals("logic")) {
            boolean l = (Boolean) left.value;
            boolean r = (Boolean) right.value;
            return switch (op) {
                case "and" -> new Value(l && r, "logic");
                case "or" -> new Value(l || r, "logic");
                default -> throw new RuntimeException("Unknown logic operator: " + op);
            };
        }
        throw new RuntimeException("Unsupported operand types for '" + op + "': " + left.type + " and " + right.type);
    }

}
//