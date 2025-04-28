package archit.common.stdlib;

import archit.common.natives.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardLibrary {
    private static final Object[] COMMON_NATIVES = {
        new MathNatives(),
    };

    private final Map<String, List<ArchitFunction>> registeredFunctions = new HashMap<>();

    public StandardLibrary() {
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
        // TODO parse
    }

    public Map<String, List<ArchitFunction>> getRegisteredFunctions() {
        return registeredFunctions;
    }
}
