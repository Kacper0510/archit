package archit.common.visitors;

import archit.common.ArchitFunction;
import archit.common.Scope;
import archit.common.Type;
import archit.parser.ArchitParser.FunctionDeclContext;
import archit.parser.ArchitParser.FunctionParamContext;
import archit.parser.ArchitParser.VarDeclContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScopeImpl implements Scope {
    private final Map<String, Variable> variables = new HashMap<>();
    private final Map<String, Set<ArchitFunction>> functions = new HashMap<>();
    private final Scope parent;

    public ScopeImpl(Scope parent) {
        this.parent = parent;
    }

    @Override
    public boolean defineVariable(String name, Type type, int id, VarDeclContext ctx) {
        return variables.put(name, new Variable(type, id, ctx)) == null;
    }

    @Override
    public boolean defineVariable(String name, Type type, int id, FunctionParamContext ctx) {
        return variables.put(name, new Variable(type, id, ctx)) == null;
    }

    @Override
    public boolean defineFunction(
        String name, Type returnType, Type[] params, String[] paramNames, FunctionDeclContext ctx
    ) {
        var function = new ArchitFunction(name, returnType, params, paramNames, false, ctx);
        functions.putIfAbsent(function.name(), new HashSet<>());
        return functions.get(function.name()).add(function);
    }

    @Override
    public ArchitFunction resolveFunction(String name, Type[] params) {
        if (functions.containsKey(name)) {
            var f =
                functions.get(name)
                    .stream()
                    .filter(x -> x.name().equals(name) && Arrays.equals(x.params(), params))
                    .findAny();
            if (f.isPresent()) {
                return f.get();
            }
        }
        return parent.resolveFunction(name, params);
    }

    @Override
    public Variable resolveVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        return parent.resolveVariable(name);
    }

    @Override
    public Scope getParent() {
        return parent;
    }
}
