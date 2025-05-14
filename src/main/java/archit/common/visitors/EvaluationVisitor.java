package archit.common.visitors;

import archit.common.Material;
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
        var symbol = ctx.symbol();
        var id = tables.getSymbols().get(symbol);
        Object value = variables.get(id);
        var op = ctx.op.getText();  //mapa ExprToOperators dopuszcza tylko expr, wiec w tym przypadku ma być tak?


        //po obliczeniu wartości zmiennej zapisuje ją do variables
        calls.add(() -> {
            Object exprValue = objects.removeLast();
            Object result;

            switch (op){
                case "=" -> {
                    variables.put(id, exprValue);
                }

                case "+=" -> {
                    if (value instanceof BigInteger && exprValue instanceof BigInteger) {
                        result = ((BigInteger) value).add((BigInteger) exprValue);
                    }
                    // w innym przypadku musi to być Double
                    else {
                        assert value instanceof Double;
                        result = ((Double) value) + ((Double) exprValue);
                    }

                    variables.put(id, result);
                }
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
            objects.add(Double.parseDouble(ctx.REAL().getText().replace("_", "")));
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

    @SuppressWarnings("unchecked")
    public void visitFunctionCall(ArchitParser.FunctionCallContext ctx) {
        ArchitFunction function = tables.getFunctions().get(ctx);

        //po obliczonych argumentach funkcji, wywołujemy ją
        calls.add(() -> {
            int n = ctx.expr().size();
            Object[] args = new Object[n];
            for (int i = 0; i < n; i++) {
                args[i] = objects.removeLast();
            }

            Object result;

            if (function.isNative()) {
                //kolejno: scriptRun, lista argumentów, typ zwracany
                BiFunction<ScriptRun, Object[], Object> impl =
                        (BiFunction<ScriptRun, Object[], Object>) function.callInfo();
                result = impl.apply(run, args);
            }

            //jesli jest skryptowa
            else{
                // TODO (emil)
            }

            //objects.add(result);
        });

        //pierw obliczamy wszystkie argumenty funkcji
        var exprs = ctx.expr();
        for (int i = exprs.size() - 1; i >= 0; i--) {
            ArchitParser.ExprContext arg = exprs.get(i);
            calls.add(() -> visitExpr(arg));
        }

        return;
    }

    public void visitFunctionCallNoBrackets(ArchitParser.FunctionCallNoBracketsContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitFunctionDecl(ArchitParser.FunctionDeclContext ctx) {
        // TODO (emil)
        return;
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
        // TODO (emil)
        return;
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
            Runnable r = () -> visitStatement(s);
            calls.add(r);
        }
    }

    public void visitStatement(ArchitParser.StatementContext ctx) {

        if(ctx.functionDecl() != null) {
            calls.add(() -> visitFunctionDecl(ctx.functionDecl()));
            return;
        }

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
            long howMany = (long) objects.removeLast();
            for (long i = 0; i < howMany ; i++) {
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

    public void visitReturnStat(ArchitParser.ReturnStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitScopeStat(ArchitParser.ScopeStatContext ctx) {
        // TODO (emil)
        return;
    }

    public void visitVarDecl(ArchitParser.VarDeclContext ctx) {
        var id = tables.getSymbols().get(ctx.symbol());

        //po obliczeniu wartości zmiennej zapisuje ją
        calls.add(() -> {
            Object value = objects.removeLast();
            variables.put(id, value);
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
        // TODO (emil)

        calls.add(() -> {
            Boolean cond = (Boolean) objects.removeLast();
            if (cond) {
                calls.add(() -> visitWhileStat(ctx));
                calls.add(() -> visitScopeStat(ctx.scopeStat()));
            }
        });

        //obliczenie warunku
        calls.add(() -> {
            if (ctx.expr() != null){
                visitExpr(ctx.expr());
            }
            else{
                visitFunctionCallNoBrackets(ctx.functionCallNoBrackets());
            }
        });
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
