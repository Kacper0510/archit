package archit.common.visitors;

import archit.common.ScriptRun;
import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationVisitor {
    private final ScriptRun run;
    private final InfoTables tables;

    //stosy
    public final Map<Integer, Object> variables = new HashMap<>();
    public final List<Runnable> calls = new ArrayList<>();
    public final List<Object> objects = new ArrayList<>();

    public EvaluationVisitor(ScriptRun run, InfoTables tables) {
        this.run = run;
        this.tables = tables;
    }

    public void visitAssignStat(ArchitParser.AssignStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitBreakStat(ArchitParser.BreakStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitElseIfStat(ArchitParser.ElseIfStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitElseStat(ArchitParser.ElseStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitEnumExpr(ArchitParser.EnumExprContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitExpr(ArchitParser.ExprContext ctx) {
        // TODO (emil)
        if (ctx.NUMBER() != null) {
            objects.add(new BigInteger(ctx.NUMBER().getText().replace("_", ""))); // "_" czyli wsparcie dla wiekszych liczb
            return;
        }

        if (ctx.REAL() != null) {
            objects.add(objects.add(Double.parseDouble(ctx.REAL().getText().replace("_", ""))));
            return;
        }

        if (ctx.LOGIC() != null) {
            objects.add(Boolean.parseBoolean(ctx.LOGIC().getText()));
            return;
        }

        if (ctx.STRING() != null) {
            String fullText = ctx.STRING().getText();
            objects.add(fullText.substring(1, fullText.length() - 1));  //usuwanie cudzysłowów
            return;
        }

        //operator
        Operators op = tables.getOperators().get(ctx);

        //Unarne
        // 1) not
        if (ctx.expr().size() == 1 && op == Operators.NOT) {
            // nadpisanie ostatniego elementu stosu objects jego negacją
            calls.add(() -> {
                Boolean v = (Boolean) objects.removeLast();
                objects.add(!v);
            });
            // rekurencja w celu pełnego obliczenia podwyrażeń expr
            calls.add(() -> visitExpr(ctx.expr(0)));
            return;
        }
        // 2) minus
        if (ctx.expr().size() == 1 && ctx.op.getText().equals("-")) {   //do zamiany na if (ctx.expr().size() == 1 && op == Operators.UNARY_MINUS)
            calls.add(() -> {
                if (objects.getLast() instanceof BigInteger) {
                    BigInteger v = (BigInteger) objects.removeLast();
                    objects.add(v.negate());
                }
                else if (objects.getLast() instanceof Double) {
                    Double v = (Double) objects.removeLast();
                    objects.add(-v);
                }
            });

            calls.add(() -> visitExpr(ctx.expr(0)));
            return;
        }


        //Binarne
        if (ctx.expr().size() == 2 && op != null) {
            var left = ctx.expr(0);
            var right = ctx.expr(1);
            calls.add(() -> {
                switch (op) {
                    case ADD_NUMBERS -> {
                        var leftResult = (BigInteger) objects.removeLast();
                        var rightResult = (BigInteger) objects.removeLast();
                        objects.add(leftResult.add(rightResult));
                    }

                    case ADD_REALS -> {
                        var leftResult = (Double) objects.removeLast();
                        var rightResult = (Double) objects.removeLast();
                        objects.add(leftResult + rightResult);
                    }

                    case SUBTRACT_NUMBERS -> {
                        var leftResult = (BigInteger) objects.removeLast();
                        var rightResult = (BigInteger) objects.removeLast();
                        objects.add(leftResult.subtract(rightResult));
                    }

                    case SUBTRACT_REALS -> {
                        var leftResult = (Double) objects.removeLast();
                        var rightResult = (Double) objects.removeLast();
                        objects.add(leftResult - rightResult);
                    }

                    case AND -> {
                        var leftResult = (Boolean) objects.removeLast();
                        var rightResult = (Boolean) objects.removeLast();
                        objects.add(leftResult && rightResult);
                    }

                    case OR -> {
                        var leftResult = (Boolean) objects.removeLast();
                        var rightResult = (Boolean) objects.removeLast();
                        objects.add(leftResult || rightResult);
                    }

                }
            });

            calls.add(() -> visitExpr(right));
            calls.add(() -> visitExpr(left));
            return;
        }
    }


    public void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitIfStat(ArchitParser.IfStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitListExpr(ArchitParser.ListExprContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitMapExpr(ArchitParser.MapExprContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitProgram(ArchitParser.ProgramContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitVarDecl(ArchitParser.VarDeclContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitWhileStat(ArchitParser.WhileStatContext ctx) {
        // TODO (emil)
        return;
    }

    /*
    // TODO (kacper) fix grammar and this function
    private String interpolateString(String text, ArchitParser.ExprContext ctx) {
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
                Value value = variableTable.getValue(varName, line);
                if (value == null) {
                    throw new ScriptExceptions.InterpolationException(
                        "Null value for variable in interpolation: " + varName
                    );
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
    */
}
