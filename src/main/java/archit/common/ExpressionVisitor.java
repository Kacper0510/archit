package archit.common;

import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitVisitor;
import archit.parser.ArchitParser;

import java.util.Map;
//
public class ExpressionVisitor extends ArchitBaseVisitor<Value> {

    private final Map<String, Value> variables;

    ExpressionVisitor(Map<String, Value> variables) {
        this.variables = variables;
    }

    @Override
    public Value visitExpr(ArchitParser.ExprContext ctx) {
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
            text = text.substring(1, text.length() - 1); // bez cudzysłowów
            return new Value(text, "string");
        }
        if (ctx.ID() != null) {
            String name = ctx.ID().getText();
            if (!variables.containsKey(name)) {
                throw new RuntimeException("Unknown variable: " + name);
            }
            return variables.get(name);
        }
        if (ctx.expr().size() == 2) {
            Value left = visit(ctx.expr(0));
            Value right = visit(ctx.expr(1));
            String op = ctx.getChild(1).getText();
            return evalBinaryOp(left, op, right);
        }
        if (ctx.expr().size() == 1) {
            Value inner = visit(ctx.expr(0));
            String op = ctx.getChild(0).getText();
            return evalUnaryOp(op, inner);
        }
        throw new RuntimeException("Unsupported expression: " + ctx.getText());
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
                default -> throw new RuntimeException("Unknown operator: " + op);
            };
        }
        if (left.type.equals("logic") && right.type.equals("logic")) {
            boolean l = left.asBoolean();
            boolean r = right.asBoolean();
            return switch (op) {
                case "and" -> new Value(l && r, "logic");
                case "or" -> new Value(l || r, "logic");
                default -> throw new RuntimeException("Unknown logic operator: " + op);
            };
        }
        throw new RuntimeException("Unsupported operand types for '" + op + "': " + left.type + " and " + right.type);
    }

    private Value evalUnaryOp(String op, Value inner) {
        if (op.equals("-")) {
            if (inner.type.equals("number")) {
                return new Value(-inner.asNumber(), "number");
            } else {
                throw new RuntimeException("Unary minus on non-number type: " + inner.type);
            }
        }
        if (op.equals("not")) {
            if (inner.type.equals("logic")) {
                return new Value(!inner.asBoolean(), "logic");
            } else {
                throw new RuntimeException("Unary not on non-logic type: " + inner.type);
            }
        }
        throw new RuntimeException("Unknown unary operator: " + op);
    }
}
