package archit.common;

import archit.common.stdlib.StandardLibrary;
import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    private final List<ScriptRun> currentRuns = new ArrayList<>();
    private final StandardLibrary standardLibrary;
    private final Logging logger;

    public Interpreter(Logging logger) {
        this.logger = logger;
        this.standardLibrary = new StandardLibrary(logger);
    }

    public List<ScriptRun> getCurrentRuns() {
        return currentRuns;
    }

    public Logging getLogger() {
        return logger;
    }

    public StandardLibrary getStandardLibrary() {
        return standardLibrary;
    }
}
