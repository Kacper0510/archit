package archit.common.visitors;

import archit.common.ScriptRun;
import archit.parser.ArchitBaseVisitor;
import archit.parser.ArchitParser;

public class EvaluationVisitor extends ArchitBaseVisitor<Object> {
    private final ScriptRun run;
    private final InfoTables tables;

    public EvaluationVisitor(ScriptRun run, InfoTables tables) {
        this.run = run;
        this.tables = tables;
    }

    @Override
    public Object visitAssignStat(ArchitParser.AssignStatContext ctx) {
        // TODO (emil)
        return super.visitAssignStat(ctx);
    }

    @Override
    public Object visitBreakStat(ArchitParser.BreakStatContext ctx) {
        // TODO (emil)
        return super.visitBreakStat(ctx);
    }

    @Override
    public Object visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        // TODO (emil)
        return super.visitContinueStat(ctx);
    }

    @Override
    public Object visitElseIfStat(ArchitParser.ElseIfStatContext ctx) {
        // TODO (emil)
        return super.visitElseIfStat(ctx);
    }

    @Override
    public Object visitElseStat(ArchitParser.ElseStatContext ctx) {
        // TODO (emil)
        return super.visitElseStat(ctx);
    }

    @Override
    public Object visitEnumExpr(ArchitParser.EnumExprContext ctx) {
        // TODO (emil)
        return super.visitEnumExpr(ctx);
    }

    @Override
    public Object visitExpr(ArchitParser.ExprContext ctx) {
        // TODO (emil)
        return super.visitExpr(ctx);
    }

    @Override
    public Object visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        // TODO (emil)
        return super.visitFunctionCall(ctx);
    }

    @Override
    public Object visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        // TODO (emil)
        return super.visitFunctionCallNoBrackets(ctx);
    }

    @Override
    public Object visitIfStat(ArchitParser.IfStatContext ctx) {
        // TODO (emil)
        return super.visitIfStat(ctx);
    }

    @Override
    public Object visitListExpr(ArchitParser.ListExprContext ctx) {
        // TODO (emil)
        return super.visitListExpr(ctx);
    }

    @Override
    public Object visitMapExpr(ArchitParser.MapExprContext ctx) {
        // TODO (emil)
        return super.visitMapExpr(ctx);
    }

    @Override
    public Object visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        // TODO (emil)
        return super.visitMaterialExpr(ctx);
    }

    @Override
    public Object visitProgram(ArchitParser.ProgramContext ctx) {
        // TODO (emil)
        return super.visitProgram(ctx);
    }

    @Override
    public Object visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        // TODO (emil)
        return super.visitRepeatStat(ctx);
    }

    @Override
    public Object visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        // TODO (emil)
        return super.visitReturnStat(ctx);
    }

    @Override
    public Object visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        // TODO (emil)
        return super.visitScopeStat(ctx);
    }

    @Override
    public Object visitVarDecl(ArchitParser.VarDeclContext ctx) {
        // TODO (emil)
        return super.visitVarDecl(ctx);
    }

    @Override
    public Object visitWhileStat(ArchitParser.WhileStatContext ctx) {
        // TODO (emil)
        return super.visitWhileStat(ctx);
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
