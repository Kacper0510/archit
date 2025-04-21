package archit.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptRun {
    /**
     * Metadata of the current run - its additional implementation specific details.
     * In Minecraft mode - {@link net.minecraft.server.command.ServerCommandSource},
     * in console mode - null.
     */
    private final Object metadata;
    private final Interpreter interpreter;
    private final Path scriptLocation;

    public ScriptRun(Interpreter interpreter, Path file, Object metadata) {
        this.interpreter = interpreter;
        this.metadata = metadata;
        this.scriptLocation = file;
    }

    public ScriptRun(Interpreter interpreter, Path file) {
        this(interpreter, file, null);
    }

    public Object getMetadata() {
        return metadata;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public Path getScriptLocation() {
        return scriptLocation;
    }

    /**
     * @return true if run was successful, false if there were any errors
     */
    public boolean run() {
        if (!Files.exists(scriptLocation)) {
            interpreter.getLogger().scriptError(this, "Script file does not exist!");
            return false;
        } else if (Files.isDirectory(scriptLocation)) {
            interpreter.getLogger().scriptError(this, "Script path leads to a directory!");
            return false;
        } else if (!Files.isReadable(scriptLocation)) {
            interpreter.getLogger().scriptError(this, "Script file is not readable!");
            return false;
        }
        String content;
        try {
            content = Files.readString(scriptLocation);
        } catch (IOException e) {
            interpreter.getLogger().systemError(e, "Failed to read the script even though it's readable?!");
            return false;
        }

        // TODO emil

        // przykładowe wywołania loggera - do usunięcia lub zakomentowania
        interpreter.getLogger().systemInfo(
            "Plik {} został uruchomiony!", scriptLocation
        );  // trafi do konsoli MC lub do log.txt
        interpreter.getLogger().scriptPrint(
            this, "Tak będzie działać funkcja print!"
        );  // trafi na chat MC lub do terminala
        var firstLine = content.split("\n")[0];
        interpreter.getLogger().scriptError(
            this, "Pierwsza linijka to {} o długości {}", firstLine, firstLine.length()
        );  // tak przekazujemy graczowi błędy

        return true;
    }
}
