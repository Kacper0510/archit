package archit.common;

// class for value and type
public class Value {
    final Object value;
    final String type;

    Value(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
