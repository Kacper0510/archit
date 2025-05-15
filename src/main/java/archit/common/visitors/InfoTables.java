package archit.common.visitors;

import archit.common.ArchitFunction;
import archit.parser.ArchitParser.AssignStatContext;
import archit.parser.ArchitParser.ExprContext;
import archit.parser.ArchitParser.FunctionCallContext;
import archit.parser.ArchitParser.FunctionCallNoBracketsContext;
import archit.parser.ArchitParser.SymbolContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.antlr.v4.runtime.ParserRuleContext;

// coś w stylu dawnego VariableTable, tylko inny format i z funkcjami
public class InfoTables {
    private final Map<SymbolContext, Integer> symbolsToIds = new HashMap<>();
    private final Map<ParserRuleContext, ArchitFunction> callsToFunctions = new HashMap<>();
    private final Map<ParserRuleContext, Operators> exprsToOperators = new HashMap<>();

    public Map<SymbolContext, Integer> getSymbols() {
        return Collections.unmodifiableMap(symbolsToIds);
    }

    public Map<ParserRuleContext, ArchitFunction> getFunctions() {
        return Collections.unmodifiableMap(callsToFunctions);
    }

    public Map<ParserRuleContext, Operators> getOperators() {
        return Collections.unmodifiableMap(exprsToOperators);
    }

    public void addSymbolMapping(SymbolContext symbol, int id) {
        symbolsToIds.put(symbol, id);
    }

    public void addFunctionMapping(FunctionCallContext call, ArchitFunction function) {
        callsToFunctions.put(call, function);
    }

    public void addFunctionMapping(FunctionCallNoBracketsContext call, ArchitFunction function) {
        callsToFunctions.put(call, function);
    }

    public void addOperatorMapping(ExprContext expr, Operators operator) {
        exprsToOperators.put(expr, operator);
    }

    public void addOperatorMapping(AssignStatContext expr, Operators operator) {
        exprsToOperators.put(expr, operator);
    }
}
