package archit.common;

public class Value {
    public Object value;
    public String type;

    public Value(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public int asNumber() {
        if (value instanceof Integer i) {
            return i;
        } else if (value instanceof Double d) {
            return d.intValue();
        }
        throw new ScriptExceptions.UnexpectedException("Value is not a number: " + value);
    }

    public boolean asBoolean() {
        if (value instanceof Boolean b) {
            return b;
        }
        throw new ScriptExceptions.UnexpectedException("Value is not a boolean: " + value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
