package archit.common;

import java.util.HashMap;
import java.util.Map;

public class VariableTable {
    private final Map<String, String> variables = new HashMap<>();

    public void declareVariable(String name, String type){
        if (!variables.containsKey(name)) {
            variables.put(name, type);
        }
        else{
            throw new VariableException("Variable '" + name + "' already exists");
        }
    }

    public String getType(String name){
        if (variables.containsKey(name)){
            return variables.get(name);
        }
        else{
            throw new VariableException("Variable '" + name + "' does not exist");
        }
    }

    public Boolean isDeclared(String name) {
        return variables.containsKey(name);
    }

    public static class VariableException extends RuntimeException {
        public VariableException(String message) {
            super(message);
        }
    }


}
