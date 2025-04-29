package archit.common;

import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        int iterations = evaluate(ctx.expr()).asNumber();
        for (int i = 0; i < iterations; i++) {
            super.visitChildren(ctx);
        }
        return null;
    }

    @Override
    public Void visitIfStat(ArchitParser.IfStatContext ctx) {
        for (int i = 0; i < ctx.expr().size(); i++) {
            Value condition = evaluate(ctx.expr(i));
            if (!condition.type.equals("logic")) {
                throw new ScriptExceptions.UnexpectedException("Condition must be a boolean expression");
            }
            if (condition.asBoolean()) {
                super.visitChildren(ctx.scopeStat(i));
                return null;
            }
        }
        super.visitChildren(ctx.scopeStat(ctx.expr().size()));
        return null;
    }

    @Override
    public Void visitVarDecl(ArchitParser.VarDeclContext ctx) {
        String varName = ctx.ID().getText();
        Value value = evaluate(ctx.expr());
        if (!variableTable.getType(varName).equals(value.type)) {
            throw new ScriptExceptions.VariableException("Type mismatch for variable: " + varName);
        }
        variableTable.setValue(varName, value);
        return null;
    }

    @Override
    public Void visitAssignStat(ArchitParser.AssignStatContext ctx) {
        String varName = ctx.ID().getText();
        Value value = evaluate(ctx.expr());
        if (!variableTable.getType(varName).equals(value.type)) {
            throw new ScriptExceptions.VariableException("Type mismatch for variable: " + varName);
        }
        variableTable.setValue(varName, value);
        return null;
    }

    @Override
    public Void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText();
        functionCall(funcName, ctx.expr());
        return null;
    }

    @Override
    public Void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        String funcName = ctx.ID().getText();
        functionCall(funcName, ctx.expr());
        return null;
    }

    private void functionCall(String functionName, List<ArchitParser.ExprContext> params) {
        switch (functionName) {
            case "print" -> {
                if (!params.isEmpty()) {
                    Value val = evaluate(params.get(0));
                    interpreter.builtinPrint(run, val.value.toString());
                }
            }
            case "move" -> {
                if (params.size() == 3) {
                    int x = evaluate(params.get(0)).asNumber();
                    int y = evaluate(params.get(1)).asNumber();
                    int z = evaluate(params.get(2)).asNumber();
                    interpreter.builtinMove(run, x, y, z);
                } else if (params.size() == 1) {
                    String direction = params.get(0).enumExpr().ID().getText();
                    switch (direction) {
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
            text = interpolateString(text);
            return new Value(text, "string");
        }
        if (ctx.ID() != null) {
            String name = ctx.ID().getText();
            if (!variableTable.isDeclared(name)) {
                throw new ScriptExceptions.VariableException("Unknown variable: " + name);
            }
            return variableTable.getValue(name);
        }
        if (ctx.expr().size() == 2) {
            Value left = evaluate(ctx.expr(0));
            Value right = evaluate(ctx.expr(1));
            String op = ctx.getChild(1).getText();
            return evalBinaryOp(left, op, right);
        }
        if (ctx.expr().size() == 1) {
            String op = ctx.getChild(0).getText();
            Value inner = evaluate(ctx.expr(0));
            if (op.equals("(")) {
                return inner;
            }
            return evalUnaryOp(op, inner);
        }
        throw new ScriptExceptions.UnexpectedException("Unsupported expression: " + ctx.getText());
    }

    private String interpolateString(String text) {
        if (text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
            text = text.substring(1, text.length() - 1);

            Pattern pattern = Pattern.compile("\\{([a-zA-Z_][a-zA-Z0-9_]*)}");
            Matcher matcher = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String varName = matcher.group(1);
                if (!variableTable.isDeclared(varName)) {
                    throw new ScriptExceptions.VariableException("Unknown variable in interpolation: " + varName);
                }
                Value value = variableTable.getValue(varName);
                if (value == null) {
                    throw new ScriptExceptions.InterpolationException("Null value for variable in interpolation: " + varName);
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value.value)));
            }

            matcher.appendTail(sb);
            return sb.toString();
        } else if (text.charAt(0) == '\'' && text.charAt(text.length() - 1) == '\'') {
            return text.substring(1, text.length() - 1);
        } else {
            throw new ScriptExceptions.SyntaxException("Invalid string format. Use either '...' or \"...\"");
        }
    }

    private Value evalBinaryOp(Value left, String op, Value right) {
        if (left.type.equals("number") && right.type.equals("number")) {
            int l = left.asNumber();
            int r = right.asNumber();
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
                default -> throw new ScriptExceptions.UnexpectedException("Unknown operator: " + op);
            };
        }
        if (left.type.equals("logic") && right.type.equals("logic")) {
            boolean l = left.asBoolean();
            boolean r = right.asBoolean();
            return switch (op) {
                case "and" -> new Value(l && r, "logic");
                case "or" -> new Value(l || r, "logic");
                default -> throw new ScriptExceptions.UnexpectedException("Unknown logic operator: " + op);
            };
        }
        throw new ScriptExceptions.UnexpectedException("Unsupported operand types for '" + op + "': " + left.type + " and " + right.type);
    }

    private Value evalUnaryOp(String op, Value inner) {
        if (op.equals("-")) {
            if (inner.type.equals("number")) {
                return new Value(-inner.asNumber(), "number");
            } else {
                throw new ScriptExceptions.UnexpectedException("Unary minus on non-number type: " + inner.type);
            }
        }
        if (op.equals("not")) {
            if (inner.type.equals("logic")) {
                return new Value(!inner.asBoolean(), "logic");
            } else {
                throw new ScriptExceptions.UnexpectedException("Unary not on non-logic type: " + inner.type);
            }
        }
        throw new ScriptExceptions.UnexpectedException("Unknown unary operator: " + op);
    }
}
