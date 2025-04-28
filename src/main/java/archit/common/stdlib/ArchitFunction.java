package archit.common.stdlib;

import archit.common.ScriptRun;
import archit.common.Type;
import java.util.Arrays;

public record ArchitFunction(String name, Type returnType, Type[] params, Callable callable) {
    @Override
    public final int hashCode() {
        Object[] fields = Arrays.copyOf(params, params.length + 2);
        fields[params.length] = name;
        fields[params.length + 1] = returnType;
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
        if (!returnType.equals(other.returnType)) {
            return false;
        }
        return Arrays.equals(params, other.params);
    }

    @FunctionalInterface
    public interface Callable {
        Object call(ScriptRun run, Object[] params);
    }
}
