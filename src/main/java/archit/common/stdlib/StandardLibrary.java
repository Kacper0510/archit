package archit.common.stdlib;

import archit.common.ArchitFunction;
import archit.common.DamerauLevenshtein;
import archit.common.Logging;
import archit.common.Scope;
import archit.common.ScriptErrorListener;
import archit.common.ScriptRun;
import archit.common.Type;
import archit.common.natives.*;
import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
import archit.parser.ArchitParser.FunctionDeclContext;
import archit.parser.ArchitParser.FunctionParamContext;
import archit.parser.ArchitParser.VarDeclContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

public class StandardLibrary implements Scope {
    private static final Object[] COMMON_NATIVES = {
        new MathNatives(), new BasicNatives(), new Casts(), new Randomness(), new StringNatives(),
    };

    private final Map<String, Set<ArchitFunction>> registeredFunctions = new HashMap<>();
    private final Map<String, Function<Type[], Optional<ArchitFunction>>> registeredDynamics = new HashMap<>();
    private final Logging logger;

    public StandardLibrary(Logging logger) {
        this.logger = logger;
        registerNatives(COMMON_NATIVES);
    }

    public void registerNatives(Object... natives) {
        for (var n : natives) {
            for (var m : n.getClass().getMethods()) {
                var isStatic = Modifier.isStatic(m.getModifiers());
                registerNative(m, isStatic ? null : n);
                registerDynamic(m, isStatic ? null : n);
            }
        }
    }

    private void registerNative(Method method, Object nativeObject) {
        if (!method.canAccess(nativeObject) || !method.isAnnotationPresent(ArchitNative.class)) {
            return;
        }
        String declaration = method.getAnnotation(ArchitNative.class).value();
        ArchitLexer lexer = new ArchitLexer(CharStreams.fromString(declaration));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ArchitParser parser = new ArchitParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ScriptErrorListener(null));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ScriptErrorListener(null));

        try {
            var tree = parser.nativeDecl();
            var function = parseNativeDecl(tree, method, nativeObject);
            registeredFunctions.putIfAbsent(function.name(), new HashSet<>());
            boolean notDuplicate = registeredFunctions.get(function.name()).add(function);
            if (notDuplicate) {
                logger.systemInfo("Successfully registered native function: {}", function.name());
            } else {
                logger.systemError("Tried to register two identical native functions: {}", function.name());
            }
        } catch (RecognitionException e) {
            logger.systemError(e, "Error while parsing native function declaration: {}", method.getName());
        } catch (TypeMismatchException e) {
            logger.systemError(
                e,
                "Error while type checking native function declaration: {}, expected: {}, found: {}",
                method.getName(),
                e.expected,
                e.found
            );
        } catch (IllegalArgumentException e) {
            logger.systemError(e, "Error in native function declaration: {}", method.getName());
        }
    }

    private ArchitFunction parseNativeDecl(  // clang-format off
        ArchitParser.NativeDeclContext ctx, Method method, Object nativeObject
    ) throws TypeMismatchException, IllegalArgumentException {
        String name = ctx.ID().getText();
        Type returnType = null;
        if (ctx.type() != null) {
            returnType = Type.fromTypeContext(ctx.type());
        }
        Type[] params = ctx.functionParams() == null
            ? new Type[0]
            : ctx.functionParams()
            .functionParam()
            .stream()
            .map(p -> p.type())
            .map(Type::fromTypeContext)
            .toArray(Type[]::new);
        String[] paramNames = ctx.functionParams() == null
            ? new String[0]
            : ctx.functionParams()
            .functionParam()
            .stream()
            .map(p -> p.symbol().ID().getText())
            .toArray(String[]::new);
        
        if (returnType != null && !returnType.getEquivalent().equals(method.getReturnType())) {
            throw new TypeMismatchException(
                "Return types do not match!", returnType.getEquivalent(), method.getReturnType()
            );
        } else if (method.getParameterCount() == 0 || !method.getParameterTypes()[0].equals(ScriptRun.class)) {
            throw new TypeMismatchException(
                "The first Java parameter should be ScriptRun!", ScriptRun.class, method.getParameterTypes()[0]
            );
        } else if (params.length != method.getParameterCount() - 1) {
            throw new IllegalArgumentException("Parameter counts do not match!");
        }
        for (int i = 0; i < params.length; i++) {
            if (!params[i].getEquivalent().equals(method.getParameterTypes()[i + 1])) {
                throw new TypeMismatchException(
                    "Parameter " + paramNames[i] + " has non-matching types!",
                    params[i].getEquivalent(),
                    method.getParameterTypes()[i + 1]
                );
            }
            for (int j = i + 1; j < params.length; j++) {
                if (paramNames[i].equals(paramNames[j])) {
                    throw new IllegalArgumentException("Duplicate parameter name: " + paramNames[i]);
                }
            }
        }

        BiFunction<ScriptRun, Object[], Object> call = (run, p) -> {
            try {
                var paramExt = new Object[1 + p.length];
                paramExt[0] = run;
                System.arraycopy(p, 0, paramExt, 1, p.length);
                return method.invoke(nativeObject, paramExt);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Native function lambda caught an exception", e);
            }
        };
        return ArchitFunction.fromFunction(name, returnType, params, call, paramNames);
    }  // clang-format on

    private void registerDynamic(Method method, Object nativeObject) {
        if (!method.canAccess(nativeObject) || !method.isAnnotationPresent(ArchitDynamic.class)) {
            return;
        }
        String customName = method.getAnnotation(ArchitDynamic.class).name();
        if (customName.isEmpty()) {
            customName = method.getName();
        }

        try {
            if (method.getGenericReturnType() instanceof ParameterizedType pt) {
                if (!pt.getRawType().equals(Optional.class)
                    || !pt.getActualTypeArguments()[0].equals(ArchitFunction.class)) {
                    throw new IllegalArgumentException("Return type must be exactly Optional<ArchitFunction>!");
                }
            } else {
                throw new IllegalArgumentException("Return type must be exactly Optional<ArchitFunction>!");
            }
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Dynamic function must take exactly one parameter, Type[]!");
            }
            if (!method.getParameterTypes()[0].equals(Type[].class)) {
                throw new TypeMismatchException(
                    "Dynamic parameter does not match Type[]!", Type[].class, method.getParameterTypes()[0]
                );
            }

            @SuppressWarnings("unchecked")
            Function<Type[], Optional<ArchitFunction>> function = types -> {
                try {
                    return (Optional<ArchitFunction>) method.invoke(nativeObject, (Object) types);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Dynamic function lambda caught an exception", e);
                }
            };

            boolean notDuplicate = registeredDynamics.putIfAbsent(customName, function) == null;
            if (notDuplicate) {
                logger.systemInfo("Successfully registered dynamic function: {}", customName);
            } else {
                logger.systemError("Tried to register two identical dynamic functions: {}", customName);
            }
        } catch (TypeMismatchException e) {
            logger.systemError(
                e,
                "Error while type checking dynamic function declaration: {}, expected: {}, found: {}",
                method.getName(),
                e.expected,
                e.found
            );
        } catch (IllegalArgumentException e) {
            logger.systemError(e, "Error in dynamic function declaration: {}", method.getName());
        }
    }

    private static class TypeMismatchException extends Exception {
        public final Class<?> expected;
        public final Class<?> found;

        public TypeMismatchException(String message, Class<?> expected, Class<?> found) {
            super(message);
            this.expected = expected;
            this.found = found;
        }
    }

    @Override
    public ArchitFunction resolveFunction(String name, Type[] params) {
        if (registeredFunctions.containsKey(name)) {
            var f =
                registeredFunctions.get(name)
                    .stream()
                    .filter(x -> x.name().equals(name) && Arrays.equals(x.params(), params))
                    .findAny();
            if (f.isPresent()) {
                return f.get();
            }
        } else if (registeredDynamics.containsKey(name)) {
            var d = registeredDynamics.get(name).apply(params);
            if (d.isPresent()) {
                return d.get();
            }
        }
        return null;
    }

    @Override
    public Variable resolveVariable(String name, int depth) {
        return null;
    }

    @Override
    public boolean defineVariable(String name, Type type, int id, VarDeclContext ctx) {
        throw new UnsupportedOperationException("Standard library cannot be extended with script-defined items");
    }

    @Override
    public boolean defineVariable(String name, Type type, int id, FunctionParamContext ctx) {
        throw new UnsupportedOperationException("Standard library cannot be extended with script-defined items");
    }

    @Override
    public boolean defineFunction(
        String name, Type returnType, Type[] params, String[] paramNames, FunctionDeclContext ctx
    ) {
        throw new UnsupportedOperationException("Standard library cannot be extended with script-defined items");
    }

    @Override
    public Scope getParent() {
        return null;
    }

    @Override
    public Set<String> getLevenshteinSuggestions(String name) {
        return Stream.concat(registeredFunctions.keySet().stream(), registeredDynamics.keySet().stream())
            .distinct()
            .map(s -> new SimpleEntry<>(s, DamerauLevenshtein.calculateDistance(s, name)))
            .filter(s -> s.getValue() < 3)
            .map(SimpleEntry::getKey)
            .collect(Collectors.toSet());
    }
}
