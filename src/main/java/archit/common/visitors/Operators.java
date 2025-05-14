package archit.common.visitors;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum Operators {
    // Arithmetic
    ADD_NUMBERS((a, b) -> ((BigInteger) a).add((BigInteger) b)),
    ADD_REALS((a, b) -> ((Double) a) + ((Double) b)),
    SUBTRACT_NUMBERS((a, b) -> ((BigInteger) a).subtract((BigInteger) b)),
    SUBTRACT_REALS((a, b) -> ((Double) a) - ((Double) b)),
    MULTIPLY_NUMBERS((a, b) -> ((BigInteger) a).multiply((BigInteger) b)),
    MULTIPLY_REALS((a, b) -> ((Double) a) * ((Double) b)),
    DIVIDE_NUMBERS((a, b) -> ((BigInteger) a).divide((BigInteger) b)),
    DIVIDE_REALS((a, b) -> ((Double) a) / ((Double) b)),
    MODULO((a, b) -> ((BigInteger) a).mod((BigInteger) b)),
    POWER_NUMBERS((a, b) -> ((BigInteger) a).pow(((BigInteger) b).intValue())),
    POWER_REALS((a, b) -> Math.pow((Double) a, (Double) b)),

    // Comparison
    EQUALS(Object::equals),
    NOT_EQUALS((a, b) -> !a.equals(b)),
    GREATER_NUMBERS((a, b) -> ((BigInteger) a).compareTo((BigInteger) b) > 0),
    GREATER_EQUALS_NUMBERS((a, b) -> ((BigInteger) a).compareTo((BigInteger) b) >= 0),
    GREATER_REALS((a, b) -> ((Double) a) > ((Double) b)),
    GREATER_EQUALS_REALS((a, b) -> ((Double) a) >= ((Double) b)),
    LESS_NUMBERS((a, b) -> ((BigInteger) a).compareTo((BigInteger) b) < 0),
    LESS_EQUALS_NUMBERS((a, b) -> ((BigInteger) a).compareTo((BigInteger) b) <= 0),
    LESS_REALS((a, b) -> ((Double) a) < ((Double) b)),
    LESS_EQUALS_REALS((a, b) -> ((Double) a) <= ((Double) b)),

    // Logical
    AND((a, b) -> ((Boolean) a) && ((Boolean) b)),
    OR((a, b) -> ((Boolean) a) || ((Boolean) b)),
    NOT((a, b) -> !((Boolean) a)),

    // Indexing / Access
    LIST_INDEX((a, b) -> ((List<?>) a).get(((BigInteger) b).intValue())),
    MAP_GET((a, b) -> ((Map<?, ?>) a).get(b)),

    // Unary minus
    NEGATE_NUMBER((a, b) -> ((BigInteger) a).negate()),
    NEGATE_REAL((a, b) -> - ((Double) a));

    private final BiFunction<Object, Object, Object> function;

    private Operators(BiFunction<Object, Object, Object> function) {
        this.function = function;
    }

    public Object apply(Object a, Object b) {
        return function.apply(a, b);
    }
}
