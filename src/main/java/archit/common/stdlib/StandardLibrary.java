package archit.common.stdlib;

import archit.common.Logging;
import archit.common.ScriptErrorListener;
import archit.common.natives.*;
import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

public class StandardLibrary {
    private static final Object[] COMMON_NATIVES = {
        new MathNatives(),
    };

    private final Map<String, List<ArchitFunction>> registeredFunctions = new HashMap<>();
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
            // todo visitor
        } catch (RecognitionException e) {
            logger.systemError(e, "Error while parsing native function declaration: {}", method.getName());
        }
    }

    public Map<String, List<ArchitFunction>> getRegisteredFunctions() {
        return registeredFunctions;
    }
}
