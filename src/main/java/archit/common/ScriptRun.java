package archit.common;

public class ScriptRun {
    /**
     * Source of the current run. In Minecraft mode - {@link net.minecraft.server.command.ServerCommandSource},
     * in console mode - null.
     */
    private final Object source;
    private final Interpreter interpreter;

    public ScriptRun(Interpreter interpreter, Object source) {
        this.interpreter = interpreter;
        this.source = source;
    }

    public ScriptRun(Interpreter interpreter) {
        this(interpreter, null);
    }

    public Object getSource() {
        return source;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }
}
