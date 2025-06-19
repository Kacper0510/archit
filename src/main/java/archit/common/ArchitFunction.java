package archit.common;

import java.util.Arrays;
import java.util.function.BiFunction;

public record ArchitFunction(  // NOSONAR
    String name,
    Type returnType,
    Type[] params,
    boolean isNative,
    Object callInfo,
    String[] paramNames
) {
    @Override
    public final int hashCode() {
        var fields = new Object[params.length + 1];
        System.arraycopy(params, 0, fields, 0, params.length);
        fields[params.length] = name;
        return Arrays.hashCode(fields);
    }

    @Override
    public final boolean equals(Object arg0) {
        if (!(arg0 instanceof ArchitFunction)) {
            return false;
        }
        ArchitFunction other = (ArchitFunction) arg0;
        if (!name.equals(other.name)) {
            return false;
        }
        return Arrays.equals(params, other.params);
    }

    public static ArchitFunction fromFunction(
        String name,
        Type returnType,
        Type[] params,
        BiFunction<ScriptRun, Object[], Object> function,
        String... paramNames
    ) {
        return new ArchitFunction(name, returnType, params, true, function, paramNames);
    }
}
