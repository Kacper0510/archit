package archit.common;

public class Value {
    public Object value;
    public String type;

    public Value(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public int asNumber() {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        throw new RuntimeException("Value is not a number: " + value);
    }

    public boolean asBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new RuntimeException("Value is not a boolean: " + value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
