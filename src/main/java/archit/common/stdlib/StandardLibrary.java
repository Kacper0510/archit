package archit.common.stdlib;

import archit.common.ArchitFunction;
import archit.common.Logging;
import archit.common.ScriptErrorListener;
import archit.common.ScriptException;
import archit.common.ScriptRun;
import archit.common.Type;
import archit.common.natives.*;
import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

public class StandardLibrary {
    private static final Object[] COMMON_NATIVES = {
        new MathNatives(),
        new BasicNatives(),
    };

    private final Map<String, Set<ArchitFunction>> registeredFunctions = new HashMap<>();
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
        }
        return new ArchitFunction(name, returnType, params, paramNames, (run, p) -> {
            try {
                var paramExt = new Object[1 + p.length];
                paramExt[0] = run;
                System.arraycopy(p, 0, paramExt, 1, p.length);
                return method.invoke(nativeObject, paramExt);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ScriptException(
                    run, ScriptException.Type.RUNTIME_ERROR, 0, 0,
                    "Native function caught an exception: {}", e.getMessage()
                );
            }
        });
    }  // clang-format on

    private static class TypeMismatchException extends Exception {
        public final Class<?> expected;
        public final Class<?> found;

        public TypeMismatchException(String message, Class<?> expected, Class<?> found) {
            super(message);
            this.expected = expected;
            this.found = found;
        }
    }

    public Map<String, Set<ArchitFunction>> getRegisteredFunctions() {
        return registeredFunctions;
    }
}
