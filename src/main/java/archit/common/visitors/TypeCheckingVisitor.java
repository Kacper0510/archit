
package archit.common.visitors;

import archit.common.*;
import archit.parser.ArchitParser;
import archit.parser.ArchitParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.stream.Collectors;

public class TypeCheckingVisitor extends ArchitParserBaseVisitor<Type> {
    private final ScriptRun run;
    private final InfoTables tables = new InfoTables();
    private Scope currentScope;
    private int nextVarId = 0;

    public TypeCheckingVisitor(ScriptRun run) {
        this.run = run;
        this.currentScope = run.getInterpreter().getStandardLibrary();
        pushScope();  // global
    }

    private void pushScope() {
        currentScope = new ScopeImpl(currentScope);
    }

    private void popScope() {
        currentScope = currentScope.getParent();
    }

    public InfoTables getTables() {
        return tables;
    }

    private void error(ArchitParser.ExprContext ctx, String fmt, Object... args) {
        throw new ScriptException(run, ScriptException.Type.TYPE_ERROR, ctx, fmt, args);
    }

    private void error(ArchitParser.SymbolContext ctx, String fmt, Object... args) {
        throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, fmt, args);
    }

    protected void error(ParserRuleContext ctx, String message, Object... args) {
        System.err.printf("ERROR at %s: ", ctx.getStart().getLine());
        System.err.printf(message, args);
        System.err.println();
    }


    @Override
    public Type visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        Type t = visit(ctx.expr());
        return t;
    }

    @Override
    public Type visitVarDecl(ArchitParser.VarDeclContext ctx) {
        String name = ctx.symbol().getText();
        Type declared = visit(ctx.type());
        // check initializer
        Type init = ctx.expr() != null
                ? visit(ctx.expr())
                : visit(ctx.functionCallNoBrackets());
        if (!init.equals(declared)) {
            error(ctx, "Cannot assign %s to variable '%s' of type %s", init, name, declared);
        }
        int id = nextVarId++;
        boolean ok = currentScope.defineVariable(name, declared, id, ctx);
        if (!ok) {
            error(ctx.symbol(), "Variable '%s' already defined in this scope", name);
        }
        tables.addSymbolMapping(ctx.symbol(), id);
        return null;
    }

    private void error(ArchitParser.VarDeclContext ctx, String s, Type init, String name, Type declared) {
    }

    @Override
    public Type visitAssignStat(ArchitParser.AssignStatContext ctx) {
        String name = ctx.symbol().getText();
        Scope.Variable var;
        try {
            var = currentScope.resolveVariable(name);
        } catch (RuntimeException e) {
            error(ctx.symbol(), "Variable '%s' not defined", name);
            return null; // unreachable
        }
        Type lhs = var.type();
        Type rhs = ctx.expr() != null
                ? visit(ctx.expr())
                : visit(ctx.functionCallNoBrackets());
        if (!rhs.equals(lhs)) {
            error(ctx, "Cannot assign %s to '%s' of type %s", rhs, name, lhs);
        }
        tables.addSymbolMapping(ctx.symbol(), var.id());
        return null;
    }

    @Override
    public Type visitSymbol(ArchitParser.SymbolContext ctx) {
        String name = ctx.ID().getText();
        Scope.Variable var;
        try {
            var = currentScope.resolveVariable(name);
        } catch (RuntimeException e) {
            throw new ScriptException(run, ScriptException.Type.NAME_ERROR,
                    ctx, "Variable '%s' not defined", name);
        }
        tables.addSymbolMapping(ctx, var.id());
        return var.type();
    }


    @Override
    public Type visitIfStat(ArchitParser.IfStatContext ctx) {
        Type cond = ctx.expr() != null
                ? visit(ctx.expr())
                : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            error(ctx, "Condition in 'if' must be logic, found %s", cond);
        }
        visit(ctx.scopeStat());
        if (ctx.elseStat() != null) {
            visitElseStat(ctx.elseStat());
        }
        return null;
    }

    @Override
    public Type visitElseStat(ArchitParser.ElseStatContext ctx) {
        visit(ctx.scopeStat());
        return null;
    }

    @Override
    public Type visitWhileStat(ArchitParser.WhileStatContext ctx) {
        Type cond = ctx.expr() != null
                ? visit(ctx.expr())
                : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            error(ctx, "Condition in 'while' must be logic, found %s", cond);
        }
        visit(ctx.scopeStat());
        return null;
    }

    @Override
    public Type visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        Type times = ctx.expr() != null
                ? visit(ctx.expr())
                : visit(ctx.functionCallNoBrackets());
        if (!times.equals(Type.number) && !times.equals(Type.real)) {
            error(ctx, "Repeat count must be number or real, found %s", times);
        }
        visit(ctx.scopeStat());
        return null;
    }

    @Override
    public Type visitFunctionDecl(ArchitParser.FunctionDeclContext ctx) {
        String name = ctx.ID().getText();
        List<ArchitParser.FunctionParamContext> params = ctx.functionParams() != null
                ? ctx.functionParams().functionParam()
                : List.of();
        Type[] paramTypes = params.stream().map(this::visit).toArray(Type[]::new);
        String[] paramNames = params.stream().map(p -> p.symbol().getText()).toArray(String[]::new);
        Type retType = ctx.type() != null ? visit(ctx.type()) : Type.number; // default?

        boolean ok = currentScope.defineFunction(name, retType, paramTypes, paramNames, ctx);
        if (!ok) {
            throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, "Function '%s' already defined", name);
        }

        pushScope();

        for (int i = 0; i < params.size(); i++) {
            int id = nextVarId++;
            var p = params.get(i);
            currentScope.defineVariable(p.symbol().getText(), paramTypes[i], id, p);
            tables.addSymbolMapping(p.symbol(), id);
        }
        visit(ctx.scopeStat());
        popScope();
        return null;
    }

    @Override
    public Type visitFunctionParam(ArchitParser.FunctionParamContext ctx) {

        return visit(ctx.type());
    }

    @Override
    public Type visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        String name = ctx.ID().getText();
        List<Type> args = ctx.expr().stream().map(this::visit).collect(Collectors.toList());
        Type[] at = args.toArray(new Type[0]);
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, "Function '%s(%s)' not found",
                    name, args);
        }
        tables.addFunctionMapping(ctx, fn);
        return fn.returnType();
    }

    @Override
    public Type visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        String name = ctx.ID().getText();
        List<Type> args = ctx.expr().stream().map(this::visit).collect(Collectors.toList());
        Type[] at = args.toArray(new Type[0]);
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, "Function '%s(%s)' not found",
                    name, args);
        }
        tables.addFunctionMapping(ctx, fn);
        return fn.returnType();
    }

    @Override
    public Type visitExpr(ArchitParser.ExprContext ctx) {

        if (ctx.NUMBER() != null)     return Type.number;
        if (ctx.REAL() != null)       return Type.real;
        if (ctx.STRING() != null)     return Type.string;
        if (ctx.LOGIC() != null)      return Type.logic;

        // material
        if (ctx.materialExpr() != null) return visit(ctx.materialExpr());
        // list/map/enum
        if (ctx.listExpr() != null)   return visit(ctx.listExpr());
        if (ctx.mapExpr() != null)    return visit(ctx.mapExpr());
        if (ctx.enumExpr() != null)   return visit(ctx.enumExpr());
        if (ctx.op != null && ctx.expr().size()==1 && ctx.getText().startsWith("(")) {
            return visit(ctx.expr(0));
        }
        // unary
        if ((ctx.op != null && (ctx.op.getText().equals("-") || ctx.op.getText().equals("not")))
                && ctx.expr().size()==1) {
            Type t = visit(ctx.expr(0));
            if (ctx.op.getText().equals("-")) {
                if (t.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_NUMBER);
                    return Type.number;
                } else if (t.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_REAL);
                    return Type.real;
                }
            } else {
                if (!t.equals(Type.logic)) error(ctx, "'not' requires logic operand");
                tables.addOperatorMapping(ctx, Operators.NOT);
                return Type.logic;
            }
        }
        // binary
        if (ctx.expr().size() == 2 && ctx.op != null) {
            Type left = visit(ctx.expr(0));
            Type right = visit(ctx.expr(1));
            String o = ctx.op.getText();
            switch (o) {
                case "+":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.ADD_NUMBERS);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.ADD_REALS);
                        return Type.real;
                    }
                    break;

                case "-":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.SUBTRACT_NUMBERS);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.SUBTRACT_REALS);
                        return Type.real;
                    }
                    break;

                case "*":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.MULTIPLY_NUMBERS);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.MULTIPLY_REALS);
                        return Type.real;
                    }
                    break;

                case "/":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.DIVIDE_NUMBERS);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.DIVIDE_REALS);
                        return Type.real;
                    }
                    break;

                case "%":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.MODULO);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.MODULO);
                        return Type.real;
                    }
                    break;

                case "^":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.POWER);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.POWER);
                        return Type.real;
                    }
                    break;

                case "and":
                case "or":
                    if (left.equals(Type.logic) && right.equals(Type.logic)) {
                        tables.addOperatorMapping(ctx, o.equals("and") ? Operators.AND : Operators.OR);
                        return Type.logic;
                    }
                    break;

                case "==":
                    if (left.equals(right)) {
                        tables.addOperatorMapping(ctx, Operators.EQUALS);
                        return Type.logic;
                    }
                    break;
                case "!=":
                    if (left.equals(right)) {
                        tables.addOperatorMapping(ctx, Operators.NOT_EQUALS);
                        return Type.logic;
                    }
                    break;

                case ">":
                    if ((left.equals(Type.number) && right.equals(Type.number))
                            || (left.equals(Type.real)   && right.equals(Type.real))) {
                        tables.addOperatorMapping(ctx, Operators.GREATER);
                        return Type.logic;
                    }
                    break;
                case ">=":
                    if ((left.equals(Type.number) && right.equals(Type.number))
                            || (left.equals(Type.real)   && right.equals(Type.real))) {
                        tables.addOperatorMapping(ctx, Operators.GREATER_EQUALS);
                        return Type.logic;
                    }
                    break;
                case "<":
                    if ((left.equals(Type.number) && right.equals(Type.number))
                            || (left.equals(Type.real)   && right.equals(Type.real))) {
                        tables.addOperatorMapping(ctx, Operators.LESS);
                        return Type.logic;
                    }
                    break;
                case "<=":
                    if ((left.equals(Type.number) && right.equals(Type.number))
                            || (left.equals(Type.real)   && right.equals(Type.real))) {
                        tables.addOperatorMapping(ctx, Operators.LESS_EQUALS);
                        return Type.logic;
                    }
                    break;

                case "[":
                    if (left.asListType() != null && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.LIST_INDEX);
                        return left.asListType().getElements();
                    }
                    break;
            }

            error(ctx, "Operator '%s' cannot be applied to types %s and %s", o, left, right);
        }


        // without brackets
        if (ctx.symbol() != null) return visit(ctx.symbol());
        if (ctx.functionCall() != null) return visit(ctx.functionCall());

        throw new UnsupportedOperationException("Unhandled expr case: " + ctx.getText());
    }

    @Override
    public Type visitListExpr(ArchitParser.ListExprContext ctx) {
        List<Type> elems = ctx.expr().stream().map(this::visit).collect(Collectors.toList());
        if (ctx.getText().startsWith("#")) {
            Type t = visit(ctx.materialExpr());
            return Type.list(t);
        } else {
            if (elems.isEmpty()) {
                error(ctx, "Cannot infer element type of empty list");
            }
            Type head = elems.get(0);
            for (Type t : elems) {
                if (!t.equals(head)) {
                    error(ctx, "List elements must all have same type, found %s and %s", head, t);
                }
            }
            return Type.list(head);
        }
    }

    @Override
    public Type visitMapExpr(ArchitParser.MapExprContext ctx) {
        var pairs = ctx.expr();
        if (pairs.isEmpty()) return Type.map(Type.number, Type.number);
        Type key0 = visit(pairs.get(0)), val0 = visit(pairs.get(1));
        for (int i = 2; i < pairs.size(); i += 2) {
            Type k = visit(pairs.get(i)), v = visit(pairs.get(i+1));
            if (!k.equals(key0) || !v.equals(val0)) {
                error(ctx, "Mapfuck it");
            }
        }
        return Type.map(key0, val0);
    }

    @Override
    public Type visitEnumExpr(ArchitParser.EnumExprContext ctx) {
        String member = ctx.ID().getText();
        return Type.literal(member);
    }

    @Override
    public Type visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        // ID? ':' ID
        return Type.material;
    }
}
