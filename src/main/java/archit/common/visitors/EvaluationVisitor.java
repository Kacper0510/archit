package archit.common.visitors;

import archit.common.Material;
import archit.common.ScriptException;
import archit.common.ScriptRun;
import archit.common.ArchitFunction;
import archit.parser.ArchitParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;

public class EvaluationVisitor {
    private final ScriptRun run;
    private final InfoTables tables;

    public static final int MAX_RECURSION_DEPTH = 100;

    //stosy
    private final List<Map<Integer, Object>> variables = new ArrayList<>();
    public final List<Runnable> calls = new ArrayList<>();
    private final List<Object> objects = new ArrayList<>();

    public EvaluationVisitor(ScriptRun run, InfoTables tables) {
        this.run = run;
        this.tables = tables;
        this.variables.add(new HashMap<>());
    }

    private void putVariable(int id, Object value) {
        for (int i = variables.size() - 1; i >= 0; i--) {
            var map = variables.get(i);
            if (map.containsKey(id)) {
                map.put(id, value);
                return;
            }
        }
        throw new ScriptException(
            run,
            ScriptException.Type.RUNTIME_ERROR,
            0, 0,
            "Variable assignment before initialization (this should never happen): {} at {}", value, id
        );
    }

    private Object getVariable(int id) {
        for (int i = variables.size() - 1; i >= 0; i--) {
            var map = variables.get(i);
            if (map.containsKey(id)) {
                return map.get(id);
            }
        }
        throw new ScriptException(
            run, ScriptException.Type.RUNTIME_ERROR, 0, 0, "Variable not found for id {} (this should never happen)", id
        );
    }

    public void visitAssignStat(ArchitParser.AssignStatContext ctx) {
        var symbol = ctx.symbol();
        var id = tables.getSymbols().get(symbol);
        Object value = getVariable(id);
        var op = tables.getOperators().get(ctx);

        //po obliczeniu wartości zmiennej zapisuje ją do variables
        calls.add(() -> {
            Object exprValue = objects.removeLast();
            if (op == Operators.DIVIDE_NUMBERS && ((BigInteger) exprValue).longValue() == 0) {
                throw new ScriptException(
                    run,
                    ScriptException.Type.RUNTIME_ERROR,
                    ctx,
                    "Division by zero"
                );
            }

            if (op == null) {
                putVariable(id, exprValue);
            } else {
                putVariable(id, op.apply(value, exprValue));
            }
        });

        //obliczanie expr albo functionCallNoBrackets
        if (ctx.expr() != null) {
            calls.add(() -> visitExpr(ctx.expr()));
        }
        //jesli nie expr() to musi być functionCallNoBrackets
        else {
            calls.add(() -> visitFunctionCallNoBrackets(ctx.functionCallNoBrackets()));
        }
    }

    public void visitBreakStat(ArchitParser.BreakStatContext ctx) {
        // TODO
    }

    public void visitContinueStat(ArchitParser.ContinueStatContext ctx) {
        // TODO
    }

    public void visitExpr(ArchitParser.ExprContext ctx) {
        if (ctx.NUMBER() != null) {
            objects.add(new BigInteger(ctx.NUMBER().getText().replace("_", ""))); // "_" czyli wsparcie dla wiekszych liczb
            return;
        }

        if (ctx.REAL() != null) {
            objects.add(Double.parseDouble(ctx.REAL().getText().replace("_", "")));
            return;
        }

        if (ctx.LOGIC() != null) {
            objects.add(Boolean.parseBoolean(ctx.LOGIC().getText()));
            return;
        }

        if (ctx.STRING() != null) {
            String fullText = ctx.STRING().getText().replace("\\\\", "\\").replace("\\'", "'");
            objects.add(fullText.substring(1, fullText.length() - 1));  //usuwanie cudzysłowów
            return;
        }

        if (ctx.enumExpr() != null) {
            objects.add(ctx.enumExpr().ID().getText());
            return;
        }

        if (ctx.interpolation() != null) {
            calls.add(() -> visitInterpolation(ctx.interpolation()));
            return;
        }

        if (ctx.symbol() != null) {
            var id = tables.getSymbols().get(ctx.symbol());
            objects.add(getVariable(id));
            return;
        }

        if (ctx.materialExpr() != null) {
            calls.add(() -> visitMaterialExpr(ctx.materialExpr()));
            return;
        }

        if (ctx.functionCall() != null) {
            calls.add(() -> visitFunctionCall(ctx.functionCall()));
            return;
        }

        //operator
        Operators op = tables.getOperators().get(ctx);

        //Unarne
        if (ctx.expr().size() == 1) {
            // nadpisanie ostatniego elementu stosu objects jego negacją
            calls.add(() -> {
                Object a = objects.removeLast();
                objects.add(op.apply(a, null));
            });
            // rekurencja w celu pełnego obliczenia podwyrażeń expr
            calls.add(() -> visitExpr(ctx.expr(0)));
            return;
        }


        //Binarne
        if (ctx.expr().size() == 2) {
            var left = ctx.expr(0);
            var right = ctx.expr(1);

            calls.add(() -> {
                var rightResult = objects.removeLast();
                var leftResult = objects.removeLast();
                if (op == Operators.DIVIDE_NUMBERS && ((BigInteger) rightResult).longValue() == 0) {
                    throw new ScriptException(run, ScriptException.Type.RUNTIME_ERROR, ctx, "Division by zero");
                }
                objects.add(op.apply(leftResult, rightResult));
            });

            calls.add(() -> visitExpr(right));
            calls.add(() -> visitExpr(left));
        }
    }

    private class ReturnPointer implements Runnable {
        @Override
        public void run() {
            variables.removeLast();  // drop current variable scope
        }
    }

    public void visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        calls.add(() -> {
            Runnable last = null;
            while (!(last instanceof ReturnPointer)) {
                last = calls.removeLast();
            }
            last.run();
        });
        if (ctx.expr() != null) {
            calls.add(() -> visitExpr(ctx.expr()));
        } else if (ctx.functionCallNoBrackets() != null) {
            calls.add(() -> visitFunctionCallNoBrackets(ctx.functionCallNoBrackets()));
        }
    }

    @SuppressWarnings("unchecked")
    public void visitFunctionCall(ArchitFunction function, List<ArchitParser.ExprContext> exprs) {
        // po obliczonych argumentach funkcji, wywołujemy ją
        int n = exprs.size();
        calls.add(() -> {
            Object[] args = new Object[n];
            for (int i = n - 1; i >= 0; i--) {
                args[i] = objects.removeLast();
            }

            if (function.isNative()) {
                // kolejno: scriptRun, lista argumentów, typ zwracany
                var impl = (BiFunction<ScriptRun, Object[], Object>) function.callInfo();
                var result = impl.apply(run, args);
                if (result != null) {
                    objects.add(result);
                }
            } else {  // jesli jest skryptowa
                var decl = (ArchitParser.FunctionDeclContext) function.callInfo();
                calls.add(new ReturnPointer());
                calls.add(() -> visitFunctionDecl(decl, args));
            }
        });

        // pierw obliczamy wszystkie argumenty funkcji
        for (int i = exprs.size() - 1; i >= 0; i--) {
            ArchitParser.ExprContext arg = exprs.get(i);
            calls.add(() -> visitExpr(arg));
        }
    }

    public void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        ArchitFunction function = tables.getFunctions().get(ctx);
        visitFunctionCall(function, ctx.expr());
    }

    public void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        ArchitFunction function = tables.getFunctions().get(ctx);
        visitFunctionCall(function, ctx.expr());
    }

    public void visitFunctionDecl(ArchitParser.FunctionDeclContext ctx, Object[] args) {
        variables.add(new HashMap<>());  // push new variable scope
        if (variables.size() > MAX_RECURSION_DEPTH) {
            throw new ScriptException(
                run,
                ScriptException.Type.RUNTIME_ERROR,
                ctx,
                "Max recursion depth of {} exceeded",
                MAX_RECURSION_DEPTH
            );
        }
        for (int i = 0; i < args.length; i++) {
            var paramCtx = ctx.functionParams().functionParam(i).symbol();
            var paramId = tables.getSymbols().get(paramCtx);
            variables.getLast().put(paramId, args[i]);
        }
        calls.add(() -> visitScopeStat(ctx.scopeStat()));
    }

    public void visitIfStat(ArchitParser.IfStatContext ctx) {
        calls.add(() -> {
            Boolean cond = (Boolean) objects.removeLast();
            if (cond) {
                //jesli true, wykonaj scope if'a i koniec
                calls.add(() -> visitScopeStat(ctx.scopeStat()));
            } else if (ctx.elseStat() != null) {
                var elseCtx = ctx.elseStat();
                if (elseCtx.scopeStat() != null) {
                    //normalny else
                    calls.add(() -> visitScopeStat(elseCtx.scopeStat()));
                } else {
                    //zagnieżdżony if
                    calls.add(() -> visitIfStat(elseCtx.ifStat()));
                }
            }
        });

        //obliczenie warunku głównego if'a
        if (ctx.expr() != null) {
            calls.add(() -> visitExpr(ctx.expr()));
        } else {
            calls.add(() -> visitFunctionCallNoBrackets(ctx.functionCallNoBrackets()));
        }
    }

    public void visitListExpr(ArchitParser.ListExprContext ctx) {
        //po wyliczeniu elementów tworzymy tą listę i dodajemy na stos objects
        calls.add(() -> {
            int count = ctx.expr().size();
            List<Object> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                list.add(objects.removeLast());
            }
            //odwracamy liste by kolejnosc byla prawidlowa
            Collections.reverse(list);
            objects.add(list);
        });


        //pierw wyliczamy wszystkie elementy listy
        for (int i = ctx.expr().size() - 1; i >= 0; i--) {
            var el = ctx.expr().get(i);
            calls.add(() -> visitExpr(el));
        }
    }

    public void visitMapExpr(ArchitParser.MapExprContext ctx) {
        // TODO
    }

    public void visitMaterialExpr(ArchitParser.MaterialExprContext ctx) {
        String namespace;
        String id;

        if (ctx.ID().size() == 2) {
            namespace = ctx.ID().get(0).getText();
            id = ctx.ID().get(1).getText();
        } else {
            namespace = Material.DEFAULT_NAMESPACE;
            id = ctx.ID().get(0).getText();
        }

        calls.add(() -> {
            Material mat = new Material(namespace, id);
            objects.add(mat);
        });
    }

    public void visitProgram(ArchitParser.ProgramContext ctx) {
        var stmts = ctx.statement();
        for (int i = stmts.size() - 1; i >= 0; i--) {
            var s = stmts.get(i);
            calls.add(() -> visitStatement(s));
        }
    }

    public void visitStatement(ArchitParser.StatementContext ctx) {
        if(ctx.functionCall() != null) {
            calls.add(() -> visitFunctionCall(ctx.functionCall()));
            return;
        }

        if(ctx.functionCallNoBrackets() != null) {
            calls.add(() -> visitFunctionCallNoBrackets(ctx.functionCallNoBrackets()));
            return;
        }

        if(ctx.varDecl() != null) {
            calls.add(() -> visitVarDecl(ctx.varDecl()));
            return;
        }

        if(ctx.assignStat() != null) {
            calls.add(() -> visitAssignStat(ctx.assignStat()));
            return;
        }

        if(ctx.ifStat() != null) {
            calls.add(() -> visitIfStat(ctx.ifStat()));
            return;
        }

        if(ctx.whileStat() != null) {
            calls.add(() -> visitWhileStat(ctx.whileStat()));
            return;
        }

        if(ctx.repeatStat() != null) {
            calls.add(() -> visitRepeatStat(ctx.repeatStat()));
            return;
        }

        if(ctx.breakStat() != null) {
            calls.add(() -> visitBreakStat(ctx.breakStat()));
            return;
        }

        if(ctx.continueStat() != null) {
            calls.add(() -> visitContinueStat(ctx.continueStat()));
            return;
        }

        if(ctx.returnStat() != null) {
            calls.add(() -> visitReturnStat(ctx.returnStat()));
            return;
        }

        if(ctx.scopeStat() != null) {
            calls.add(() -> visitScopeStat(ctx.scopeStat()));
        }
    }

    public void visitRepeatStat(ArchitParser.RepeatStatContext ctx) {

        calls.add(() -> {
            var howMany = (BigInteger) objects.removeLast();
            for (long i = 0; i < howMany.longValue(); i++) {
                calls.add(() -> {
                    visitScopeStat(ctx.scopeStat());
                });
            }
        });


        //obliczanie wyrażenia
        calls.add(() -> {
            if (ctx.expr() != null){
                visitExpr(ctx.expr());
            }
            else{
                visitFunctionCallNoBrackets(ctx.functionCallNoBrackets());
            }
        });
    }

    public void visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        var stmts = ctx.statement();
        for (int i = stmts.size() - 1; i >= 0; i--) {
            var s = stmts.get(i);
            calls.add(() -> visitStatement(s));
        }
    }

    public void visitVarDecl(ArchitParser.VarDeclContext ctx) {
        var id = tables.getSymbols().get(ctx.symbol());

        //po obliczeniu wartości zmiennej zapisuje ją
        calls.add(() -> {
            Object value = objects.removeLast();
            variables.getLast().put(id, value);
        });

        //pierw ma obliczyć wartość zmiennej
        if (ctx.expr() != null) {
            calls.add(() -> visitExpr(ctx.expr()));
        }
        //jesli nie expr() to musi być functionCallNoBrackets
        else {
            calls.add(() -> visitFunctionCallNoBrackets(ctx.functionCallNoBrackets()));
        }
    }

    public void visitWhileStat(ArchitParser.WhileStatContext ctx) {
        calls.add(() -> {
            Boolean cond = (Boolean) objects.removeLast();
            if (cond) {
                calls.add(() -> visitWhileStat(ctx));
                calls.add(() -> visitScopeStat(ctx.scopeStat()));
            }
        });

        // obliczenie warunku
        calls.add(() -> {
            if (ctx.expr() != null) {
                visitExpr(ctx.expr());
            } else {
                visitFunctionCallNoBrackets(ctx.functionCallNoBrackets());
            }
        });
    }

    public void visitInterpolation(ArchitParser.InterpolationContext ctx) {
        calls.add(() -> {
            List<Object> exprs = new ArrayList<>();
            for (int i = 0; i < ctx.expr().size(); i++) {
                exprs.add(objects.removeLast());
            }

            StringBuilder sb = new StringBuilder();
            for (var c : ctx.children) {
                if (c instanceof TerminalNode t) {
                    if (t.getSymbol().getType() == ArchitParser.INTER_CONTENT) {
                        sb.append(t.getText());
                    } else if (t.getSymbol().getType() == ArchitParser.INTER_ESCAPE) {
                        sb.append(t.getText().replace("\\\\", "\\").replace("\\'", "'"));
                    }
                } else if (c instanceof ArchitParser.ExprContext) {
                    sb.append(exprs.removeLast());
                }
            }
            objects.add(sb.toString());
        });
        for (int i = ctx.expr().size() - 1; i >= 0; i--) {
            var expr = ctx.expr(i);
            calls.add(() -> visitExpr(expr));
        }
    }
}
