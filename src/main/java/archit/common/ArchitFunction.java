package archit.common;

import java.util.Arrays;

public record ArchitFunction(  // NOSONAR
    String name,
    Type returnType,
    Type[] params,
    String[] paramNames,
    boolean isNative,
    Object callInfo
) {
    @Override
    public final int hashCode() {
        var fields = new Object[params.length + 2];
        System.arraycopy(params, 0, fields, 0, params.length);
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
}
