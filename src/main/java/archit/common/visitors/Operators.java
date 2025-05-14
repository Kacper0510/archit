package archit.common.visitors;

public enum Operators {
    // Arithmetic
    ADD_NUMBERS, ADD_REALS,
    SUBTRACT_NUMBERS, SUBTRACT_REALS,
    MULTIPLY_NUMBERS, MULTIPLY_REALS,
    DIVIDE_NUMBERS, DIVIDE_REALS,
    MODULO, POWER_NUMBERS, POWER_REALS,

    // Comparison
    EQUALS, NOT_EQUALS,
    GREATER, GREATER_EQUALS,
    LESS, LESS_EQUALS,

    // Logical
    AND, OR, NOT,

    // Indexing / Access
    LIST_INDEX, MAP_GET,

    // Unary minus
    NEGATE_NUMBER, NEGATE_REAL,
}
