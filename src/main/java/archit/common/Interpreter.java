package archit.common;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    private List<ScriptRun> currentRuns = new ArrayList<>();
    private final Logging logger;
    
    public Interpreter(Logging logger) {
        this.logger = logger;
    }

    public List<ScriptRun> getCurrentRuns() {
        return currentRuns;
    }

    public Logging getLogger() {
        return logger;
    }
}
