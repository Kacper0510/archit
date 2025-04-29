package archit.common;

import java.util.HashMap;
import java.util.Map;

public class VariableTable {
    private final Map<String, Value> variables = new HashMap<>();

    public void declareVariable(String name, String type, int line) {
        if (!variables.containsKey(name)) {
            variables.put(name, new Value(null, type, line));
        } else {
            var variable = variables.get(name);
            throw new ScriptExceptions.VariableException("Variable '" + name + "' already exists at line " + variable.line + ", current line " + line);
        }
    }

    public String getType(String name, int line) {
        if (variables.containsKey(name)) {
            return variables.get(name).type;
        } else {
            throw new ScriptExceptions.VariableException("Variable '" + name + "' does not exist at line " + line);
        }
    }

    public Value getValue(String name, int line) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            throw new ScriptExceptions.VariableException("Variable '" + name + "' does not exist at line " + line);
        }
    }

    public void setValue(String name, Value value, int line) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else {
            throw new ScriptExceptions.VariableException("Variable '" + name + "' does not exist at line " + line);
        }
    }

    public Boolean isDeclared(String name) {
        return variables.containsKey(name);
    }
}
