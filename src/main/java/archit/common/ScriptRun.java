package archit.common;

import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
import net.minecraft.server.command.ServerCommandSource;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

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
    private int cursorX = 0, cursorY = 0, cursorZ = 0;  // domyślne wartości wirtualnego kursora

    public ScriptRun(Interpreter interpreter, Path file, Object metadata) {
        this.interpreter = interpreter;
        this.metadata = metadata;
        this.scriptLocation = file;
    }

    // getter i setter dla wirtualnego kursora
    public void setCursor(int x, int y, int z) {
        this.cursorX = x; this.cursorY = y; this.cursorZ = z;
    }
    public int getCursorX() { return cursorX; }
    public int getCursorY() { return cursorY; }
    public int getCursorZ() { return cursorZ; }

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
    public boolean run() throws IOException {
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
        CharStream charStream = CharStreams.fromPath(scriptLocation);
        ArchitLexer lexer = new ArchitLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ArchitParser parser = new ArchitParser(tokens);
        ArchitParser.ProgramContext tree = parser.program();

        // stworzenie visitora i uruchomienie
        ArchitVisitor visitor = new ArchitVisitor(interpreter, this);
        visitor.visit(tree);

//        // przykładowe wywołania loggera - do usunięcia lub zakomentowania
//        interpreter.getLogger().systemInfo(
//            "Plik {} został uruchomiony!", scriptLocation
//        );  // trafi do konsoli MC lub do log.txt
//        interpreter.getLogger().scriptPrint(
//            this, "Tak będzie działać funkcja print!"
//        );  // trafi na chat MC lub do terminala
//        var firstLine = content.split("\n")[0];
//        interpreter.getLogger().scriptError(
//            this, "Pierwsza linijka to {} o długości {}", firstLine, firstLine.length()
//        );  // tak przekazujemy graczowi błędy

        return true;
    }
}
