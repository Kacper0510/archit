package archit.common.visitors;

import archit.common.*;
import archit.parser.ArchitParser;
import archit.parser.ArchitParserBaseVisitor;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;

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

    private void error(ArchitParser.SymbolContext ctx, String fmt, Object... args) {
        throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, fmt, args);
    }

    private void error(ParserRuleContext ctx, String message, Object... args) {
        throw new ScriptException(run, ScriptException.Type.TYPE_ERROR, ctx, message, args);
    }

    @Override
    public Type visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        pushScope();
        visitChildren(ctx);
        popScope();
        return null;
    }

    @Override
    public Type visitType(ArchitParser.TypeContext ctx) {
        return Type.fromTypeContext(ctx);
    }

    @Override
    public Type visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        Type returnType = visit(ctx.expr());
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !(parent instanceof ArchitParser.FunctionDeclContext)) {
            parent = parent.getParent();
        }
        if (!(parent instanceof ArchitParser.FunctionDeclContext func)) {
            error(ctx, "Return statement not inside a function");
            return returnType;
        }
        Type declaredReturn = visit(func.type());
        if (!declaredReturn.equals(returnType)) {
            error(ctx, "Return type mismatch: expected {}, found {}", declaredReturn, returnType);
        }
        return returnType;
    }

    @Override
    public Type visitVarDecl(ArchitParser.VarDeclContext ctx) {
        String name = ctx.symbol().getText();
        Type declared = visit(ctx.type());
        // check initializer
        Type init = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!init.equals(declared)) {
            error(ctx, "Cannot assign {} to variable '{}' of type {}", init, name, declared);
        }
        int id = nextVarId++;
        boolean ok = currentScope.defineVariable(name, declared, id, ctx);
        if (!ok) {
            error(ctx.symbol(), "Variable '{}' already defined in this scope", name);
        }
        tables.addSymbolMapping(ctx.symbol(), id);
        return null;
    }

    @Override
    public Type visitAssignStat(ArchitParser.AssignStatContext ctx) {
        String name = ctx.symbol().getText();
        Scope.Variable varRes = currentScope.resolveVariable(name);
        if (varRes == null) {
            error(ctx.symbol(), "Variable '{}' not defined", name);
        }

        Type lhs = varRes.type();
        Type rhs = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        tables.addSymbolMapping(ctx.symbol(), varRes.id());
        String op = ctx.op.getText();
        switch (op) {
            case "=" -> {
                if (lhs.equals(rhs)) {
                    return null;
                }
            }
            case "+=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.ADD_NUMBERS);
                    return null;
                } else if (lhs.equals(Type.real) && rhs.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.ADD_REALS);
                    return null;
                }
            }
            case "-=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.SUBTRACT_NUMBERS);
                    return null;
                } else if (lhs.equals(Type.real) && rhs.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.SUBTRACT_REALS);
                    return null;
                }
            }
            case "*=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.MULTIPLY_NUMBERS);
                    return null;
                } else if (lhs.equals(Type.real) && rhs.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.MULTIPLY_REALS);
                    return null;
                }
            }
            case "/=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.DIVIDE_NUMBERS);
                    return null;
                } else if (lhs.equals(Type.real) && rhs.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.DIVIDE_REALS);
                    return null;
                }
            }
            case "%=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.MODULO);
                    return null;
                }
            }
            case "^=" -> {
                if (lhs.equals(Type.number) && rhs.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.POWER_NUMBERS);
                    return null;
                } else if (lhs.equals(Type.real) && rhs.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.POWER_REALS);
                    return null;
                }
            }
        }
        error(ctx, "Cannot use operator {} with type {} to '{}' of type {}", op, rhs, name, lhs);
        return null;
    }

    @Override
    public Type visitSymbol(ArchitParser.SymbolContext ctx) {
        String name = ctx.ID().getText();
        Scope.Variable varRes = currentScope.resolveVariable(name);
        if (varRes == null) {
            error(ctx, "Variable '{}' not defined", name);
        }
        tables.addSymbolMapping(ctx, varRes.id());
        return varRes.type();
    }

    @Override
    public Type visitIfStat(ArchitParser.IfStatContext ctx) {
        Type cond = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            error(ctx, "Condition in 'if' must be logic, found {}", cond);
        }
        visit(ctx.scopeStat());
        if (ctx.elseStat() != null) {
            visitElseStat(ctx.elseStat());
        }
        return null;
    }

    @Override
    public Type visitElseStat(ArchitParser.ElseStatContext ctx) {
        if(ctx.scopeStat() != null){
            visit(ctx.scopeStat());
        }
        else{
            visit(ctx.ifStat());
        }

        return null;
    }

    @Override
    public Type visitWhileStat(ArchitParser.WhileStatContext ctx) {
        Type cond = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            error(ctx, "Condition in 'while' must be logic, found {}", cond);
        }
        visit(ctx.scopeStat());
        return null;
    }

    @Override
    public Type visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        Type times = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!times.equals(Type.number)) {
            error(ctx, "Repeat count must be number, found {}", times);
        }
        visit(ctx.scopeStat());
        return null;
    }

    @Override
    public Type visitFunctionDecl(ArchitParser.FunctionDeclContext ctx) {
        String name = ctx.ID().getText();
        List<ArchitParser.FunctionParamContext> params =
            ctx.functionParams() != null ? ctx.functionParams().functionParam() : List.of();
        Type[] paramTypes = params.stream().map(this::visit).toArray(Type[] ::new);
        String[] paramNames = params.stream().map(p -> p.symbol().getText()).toArray(String[] ::new);
        Type retType = ctx.type() != null ? visit(ctx.type()) : null;

        if (retType != null && ctx.scopeStat().statement().getLast().repeatStat() == null) {
            error(ctx, "No return statement at the end of '{}', which must return something", name);
        } 

        boolean ok = currentScope.defineFunction(name, retType, paramTypes, paramNames, ctx);
        if (!ok) {
            throw new ScriptException(run, ScriptException.Type.NAME_ERROR, ctx, "Function '{}' already defined", name);
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
        Type[] at = ctx.expr().stream().map(this::visit).toArray(Type[] ::new);
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(
                run,
                ScriptException.Type.NAME_ERROR,
                ctx,
                "Function '{}({})' not found",
                name,
                String.join(", ", Arrays.stream(at).map(Type::toString).toList())
            );
        }
        tables.addFunctionMapping(ctx, fn);
        return fn.returnType();
    }

    @Override
    public Type visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        String name = ctx.ID().getText();
        Type[] at = ctx.expr().stream().map(this::visit).toArray(Type[] ::new);
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(
                run,
                ScriptException.Type.NAME_ERROR,
                ctx,
                "Function '{}({})' not found",
                name,
                Arrays.stream(at).map(Type::toString).toList()
            );
        }
        tables.addFunctionMapping(ctx, fn);
        return fn.returnType();
    }

    @Override
    public Type visitExpr(ArchitParser.ExprContext ctx) {
        if (ctx.NUMBER() != null) return Type.number;
        if (ctx.REAL() != null) return Type.real;
        if (ctx.STRING() != null) return Type.string;
        if (ctx.LOGIC() != null) return Type.logic;

        // material
        if (ctx.materialExpr() != null) return visit(ctx.materialExpr());
        // list/map/enum
        if (ctx.listExpr() != null) return visit(ctx.listExpr());
        if (ctx.mapExpr() != null) return visit(ctx.mapExpr());
        if (ctx.enumExpr() != null) return visit(ctx.enumExpr());
        if (ctx.op != null && ctx.expr().size() == 1 && ctx.getText().startsWith("(")) {
            return visit(ctx.expr(0));
        }
        // unary
        if ((ctx.op != null && (ctx.op.getText().equals("-") || ctx.op.getText().equals("not")))
            && ctx.expr().size() == 1) {
            Type t = visit(ctx.expr(0));
            if (ctx.op.getText().equals("-")) {
                if (t.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_NUMBER);
                    return Type.number;
                } else if (t.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_REAL);
                    return Type.real;
                } else {
                    error(ctx, "Wrong type for unary minus: {}", t);
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
                    }
                    break;

                case "^":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.POWER_NUMBERS);
                        return Type.number;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.POWER_REALS);
                        return Type.real;
                    }
                    break;

                case "and", "or":
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
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.GREATER_NUMBERS);
                        return Type.logic;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.GREATER_REALS);
                        return Type.logic;
                    }
                    break;

                case ">=":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.GREATER_EQUALS_NUMBERS);
                        return Type.logic;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.GREATER_EQUALS_REALS);
                        return Type.logic;
                    }
                    break;

                case "<":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.LESS_NUMBERS);
                        return Type.logic;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.LESS_REALS);
                        return Type.logic;
                    }
                    break;

                case "<=":
                    if (left.equals(Type.number) && right.equals(Type.number)) {
                        tables.addOperatorMapping(ctx, Operators.LESS_EQUALS_NUMBERS);
                        return Type.logic;
                    } else if (left.equals(Type.real) && right.equals(Type.real)) {
                        tables.addOperatorMapping(ctx, Operators.LESS_EQUALS_REALS);
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

            error(ctx, "Operator '{}' cannot be applied to types {} and {}", o, left, right);
        }

        // without brackets
        if (ctx.symbol() != null) return visit(ctx.symbol());
        if (ctx.functionCall() != null) return visit(ctx.functionCall());
        if (ctx.interpolation() != null) {
            visit(ctx.interpolation());
            return Type.string;
        }

        throw new UnsupportedOperationException("Unhandled expr case: " + ctx.getText());
    }

    @Override
    public Type visitListExpr(ArchitParser.ListExprContext ctx) {
        List<Type> elems = ctx.expr().stream().map(this::visit).toList();
        if (ctx.getText().startsWith("#")) {
            return Type.list(Type.material);
        } else {
            if (elems.isEmpty()) {
                error(ctx, "Cannot infer element type of empty list");  // TODO: allow empty list
            }
            Type head = elems.get(0);
            for (Type t : elems) {
                if (!t.equals(head)) {
                    error(ctx, "List elements must all have same type, found {} and {}", head, t);
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
            Type k = visit(pairs.get(i)), v = visit(pairs.get(i + 1));
            if (!k.equals(key0) || !v.equals(val0)) {
                error(ctx, "Inconsistent map element types: expected ({}, {}), found ({}, {})", key0, val0, k, v);
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
