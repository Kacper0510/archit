package archit.common;

public class ScriptRun {
    /**
     * Metadata of the current run - its additional implementation specific details.
     * In Minecraft mode - {@link net.minecraft.server.command.ServerCommandSource},
     * in console mode - null.
     */
    private final Object metadata;
    private final Interpreter interpreter;
    private final String scriptLocation;

    public ScriptRun(Interpreter interpreter, String file, Object metadata) {
        this.interpreter = interpreter;
        this.metadata = metadata;
        this.scriptLocation = file;
    }

    public ScriptRun(Interpreter interpreter, String file) {
        this(interpreter, file, null);
    }

    public Object getMetadata() {
        return metadata;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public String getScriptLocation() {
        return scriptLocation;
    }
}
