package archit.common.visitors;

import archit.common.*;
import archit.parser.ArchitParser;
import archit.parser.ArchitParserBaseVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.antlr.v4.runtime.ParserRuleContext;

import static archit.common.ScriptException.Type.*;

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
        Type returnType = ctx.expr() == null ? null : visit(ctx.expr());
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !(parent instanceof ArchitParser.FunctionDeclContext)) {
            parent = parent.getParent();
        }
        if (!(parent instanceof ArchitParser.FunctionDeclContext func)) {
            throw new ScriptException(run, LOGIC_ERROR, ctx, "Return statement not inside a function");
        }
        Type declaredReturn = func.type() == null ? null : visit(func.type());
        if (returnType.equals(Type.emptyList) && declaredReturn.asListType() != null) {
            returnType = declaredReturn;
        } else if (returnType.equals(Type.emptyMap) && declaredReturn.asMapType() != null) {
            returnType = declaredReturn;
        }

        if (!Objects.equals(declaredReturn, returnType)) {
            throw new ScriptException(
                run, TYPE_ERROR, ctx, "Return type mismatch: expected {}, found {}", declaredReturn, returnType
            );
        }
        return returnType;
    }

    @Override
    public Type visitVarDecl(ArchitParser.VarDeclContext ctx) {
        String name = ctx.symbol().getText();
        if (name.contains("~")) {
            throw new ScriptException(
                run, NAME_ERROR, ctx.symbol(), "Variable declarations cannot use '~'"
            );
        }
        Type declared = visit(ctx.type());
        // check initializer
        Type init = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (init.equals(Type.emptyList) && declared.asListType() != null) {
            init = declared;
        } else if (init.equals(Type.emptyMap) && declared.asMapType() != null) {
            init = declared;
        }

        if (!Objects.equals(init, declared)) {
            throw new ScriptException(
                run, TYPE_ERROR, ctx, "Cannot assign {} to variable '{}' of type {}", init, name, declared
            );
        }
        int id = nextVarId++;
        boolean ok = currentScope.defineVariable(name, declared, id, ctx);
        if (!ok) {
            throw new ScriptException(
                run, NAME_ERROR, ctx.symbol(), "Variable '{}' already defined in this scope", name
            );
        }
        tables.addSymbolMapping(ctx.symbol(), id);
        return null;
    }

    @Override
    public Type visitAssignStat(ArchitParser.AssignStatContext ctx) {
        Type lhs = visitSymbol(ctx.symbol());
        Type rhs = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (rhs.equals(Type.emptyList) && lhs.asListType() != null) {
            rhs = lhs;
        } else if (rhs.equals(Type.emptyMap) && lhs.asMapType() != null) {
            rhs = lhs;
        }

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
        throw new ScriptException(
            run, TYPE_ERROR, ctx, "Cannot assign with {} to '{}' - {} to {}", op, ctx.symbol().getText(), rhs, lhs
        );
    }

    @Override
    public Type visitSymbol(ArchitParser.SymbolContext ctx) {
        String name = ctx.ID().getText();
        int parentLevels = ctx.children.size() - 1;
        Scope.Variable varRes = currentScope.resolveVariable(name, parentLevels);
        if (varRes == null) {
            var suggestions = currentScope.getLevenshteinSuggestions(name);
            if (suggestions.contains(name)) {
                throw new ScriptException(run, NAME_ERROR, ctx, "Too many '~' prefixes");
            } else if (suggestions.isEmpty()) {
                throw new ScriptException(run, NAME_ERROR, ctx, "Variable '{}' not defined", name);
            } else {
                throw new ScriptException(
                    run,
                    NAME_ERROR,
                    ctx,
                    "Variable '{}' not defined - did you mean any of these: {}",
                    name,
                    String.join(", ", suggestions)
                );
            }
        }
        tables.addSymbolMapping(ctx, varRes.id());
        return varRes.type();
    }

    @Override
    public Type visitIfStat(ArchitParser.IfStatContext ctx) {
        Type cond = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            throw new ScriptException(run, TYPE_ERROR, ctx, "Condition in 'if' must be logic, found {}", cond);
        }
        visit(ctx.scopeStat());
        if (ctx.elseStat() != null) {
            visitElseStat(ctx.elseStat());
        }
        return null;
    }

    @Override
    public Type visitElseStat(ArchitParser.ElseStatContext ctx) {
        if (ctx.scopeStat() != null) {
            visit(ctx.scopeStat());
        } else {
            visit(ctx.ifStat());
        }

        return null;
    }

    @Override
    public Type visitWhileStat(ArchitParser.WhileStatContext ctx) {
        Type cond = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!cond.equals(Type.logic)) {
            throw new ScriptException(run, TYPE_ERROR, ctx, "Condition in 'while' must be logic, found {}", cond);
        }
        visit(ctx.scopeStat());
        DeadCodeVisitor.checkLoop(ctx, run);
        return null;
    }

    @Override
    public Type visitRepeatStat(ArchitParser.RepeatStatContext ctx) {
        Type times = ctx.expr() != null ? visit(ctx.expr()) : visit(ctx.functionCallNoBrackets());
        if (!times.equals(Type.number)) {
            throw new ScriptException(run, TYPE_ERROR, ctx, "Repeat count must be number, found {}", times);
        }
        visit(ctx.scopeStat());
        DeadCodeVisitor.checkLoop(ctx, run);
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

        for (int i = 0; i < paramNames.length; i++) {
            for (int j = i + 1; j < paramNames.length; j++) {
                if (paramNames[i].equals(paramNames[j])) {
                    throw new ScriptException(
                        run,
                        NAME_ERROR,
                        ctx.functionParams().functionParam(j),
                        "Duplicate parameter name: {}",
                        paramNames[i]
                    );
                }
            }
        }

        DeadCodeVisitor.checkFunction(ctx, retType == null, run);

        boolean ok = currentScope.defineFunction(name, retType, paramTypes, paramNames, ctx);
        if (!ok) {
            throw new ScriptException(run, NAME_ERROR, ctx, "Function '{}' already defined", name);
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
        if (Arrays.stream(at).anyMatch(t -> t.getKind() == Type.Kind.PSEUDO)) {
            throw new ScriptException(
                run,
                TYPE_ERROR,
                ctx,
                "Empty collections not allowed in function calls, to use them, assign them to a variable first"
            );
        }
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(
                run,
                NAME_ERROR,
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
        if (Arrays.stream(at).anyMatch(t -> t.getKind() == Type.Kind.PSEUDO)) {
            throw new ScriptException(
                run,
                TYPE_ERROR,
                ctx,
                "Empty collections not allowed in function calls, to use them, assign them to a variable first"
            );
        }
        ArchitFunction fn = currentScope.resolveFunction(name, at);
        if (fn == null) {
            throw new ScriptException(
                run,
                NAME_ERROR,
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
            tables.addOperatorMapping(ctx, Operators.NO_OP);
            return visit(ctx.expr(0));
        }
        // unary
        if ((ctx.op != null && (ctx.op.getText().equals("-") || ctx.op.getText().equals("not")))
            && ctx.expr().size() == 1) {
            Type t = visit(ctx.expr(0));
            if (t.getKind() == Type.Kind.PSEUDO) {
                throw new ScriptException(
                    run,
                    TYPE_ERROR,
                    ctx,
                    "Empty collections not allowed in expressions, to use them, assign them to a variable first"
                );
            }
            if (ctx.op.getText().equals("-")) {
                if (t.equals(Type.number)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_NUMBER);
                    return Type.number;
                } else if (t.equals(Type.real)) {
                    tables.addOperatorMapping(ctx, Operators.NEGATE_REAL);
                    return Type.real;
                } else {
                    throw new ScriptException(run, TYPE_ERROR, ctx, "Wrong type for unary minus: {}", t);
                }
            } else {
                if (!t.equals(Type.logic)) {
                    throw new ScriptException(run, TYPE_ERROR, ctx, "'not' requires logic operand, found {}", t);
                }
                tables.addOperatorMapping(ctx, Operators.NOT);
                return Type.logic;
            }
        }
        // binary
        if (ctx.expr().size() == 2 && ctx.op != null) {
            Type left = visit(ctx.expr(0));
            Type right = visit(ctx.expr(1));
            if (left.getKind() == Type.Kind.PSEUDO || right.getKind() == Type.Kind.PSEUDO) {
                throw new ScriptException(
                    run,
                    TYPE_ERROR,
                    ctx,
                    "Empty collections not allowed in expressions, to use them, assign them to a variable first"
                );
            }
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
                    var mt = left.asMapType();
                    if (mt != null && right.equals(mt.getKey())) {
                        tables.addOperatorMapping(ctx, Operators.MAP_GET);
                        return mt.getValue();
                    }
                    break;
            }

            throw new ScriptException(
                run, TYPE_ERROR, ctx, "Operator '{}' cannot be applied to types {} and {}", o, left, right
            );
        }

        // without brackets
        if (ctx.symbol() != null) return visit(ctx.symbol());
        if (ctx.functionCall() != null) {
            var ret = visit(ctx.functionCall());
            if (ret == null) {
                throw new ScriptException(
                    run,
                    TYPE_ERROR,
                    ctx,
                    "Non-returning function '{}' cannot be used in expression",
                    ctx.functionCall().ID().getText()
                );
            }
            return ret;
        }
        if (ctx.interpolation() != null) {
            visit(ctx.interpolation());
            return Type.string;
        }

        throw new UnsupportedOperationException("Unhandled expr case: " + ctx.getText());
    }

    @Override
    public Type visitInterpolation(ArchitParser.InterpolationContext ctx) {
        for (ArchitParser.ExprContext expr : ctx.expr()) {
            Type t = visit(expr);
            if (t.getKind() != Type.Kind.SIMPLE) {
                throw new ScriptException(run, TYPE_ERROR, expr, "Only simple types can be interpolated, found {}", t);
            }
        }
        return null;
    }

    @Override
    public Type visitListExpr(ArchitParser.ListExprContext ctx) {
        List<Type> elems = ctx.expr().stream().map(this::visit).toList();
        if (ctx.getText().startsWith("#")) {
            return Type.list(Type.material);
        }
        List<Type> nonPseudoElems = elems.stream().filter(t -> t.getKind() != Type.Kind.PSEUDO).toList();
        if (nonPseudoElems.isEmpty()) {
            return Type.emptyList;
        }
        Type head = nonPseudoElems.get(0);
        for (Type t : elems) {
            if (!t.equals(head)) {
                throw new ScriptException(
                    run, TYPE_ERROR, ctx, "List elements must all have same type, found {} and {}", head, t
                );
            }
        }
        return Type.list(head);
    }

    @Override
    public Type visitMapExpr(ArchitParser.MapExprContext ctx) {
        List<Type> keys = new ArrayList<>();
        List<Type> values = new ArrayList<>();
        for (int i = 0; i < ctx.expr().size(); i += 2) {
            keys.add(visit(ctx.expr().get(i)));
            values.add(visit(ctx.expr().get(i + 1)));
        }
        var nonPseudoKeys = keys.stream().filter(t -> t.getKind() != Type.Kind.PSEUDO).toList();
        var nonPseudoValues = values.stream().filter(t -> t.getKind() != Type.Kind.PSEUDO).toList();
        if (nonPseudoKeys.isEmpty() || nonPseudoValues.isEmpty()) {
            return Type.emptyMap;
        }
        Type keyType = nonPseudoKeys.get(0);
        Type valueType = nonPseudoValues.get(0);
        for (Type t : keys) {
            if (!t.equals(keyType)) {
                throw new ScriptException(
                    run, TYPE_ERROR, ctx, "Map keys must all have same type, found {} and {}", keyType, t
                );
            }
        }
        for (Type t : values) {
            if (!t.equals(valueType)) {
                throw new ScriptException(
                    run, TYPE_ERROR, ctx, "Map values must all have same type, found {} and {}", valueType, t
                );
            }
        }
        return Type.map(keyType, valueType);
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

    private static Predicate<ParserRuleContext> IS_LOOP =
        p -> p instanceof ArchitParser.WhileStatContext || p instanceof ArchitParser.RepeatStatContext;

    @Override
    public Type visitBreakStat(ArchitParser.BreakStatContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !IS_LOOP.test(parent)) {
            parent = parent.getParent();
        }
        if (!IS_LOOP.test(parent)) {
            throw new ScriptException(run, LOGIC_ERROR, ctx, "Break statement not inside a loop");
        }
        return null;
    }

    @Override
    public Type visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null && !IS_LOOP.test(parent)) {
            parent = parent.getParent();
        }
        if (!IS_LOOP.test(parent)) {
            throw new ScriptException(run, LOGIC_ERROR, ctx, "Continue statement not inside a loop");
        }
        return null;
    }
}
