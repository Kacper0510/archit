package archit.common;

import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
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
    private int cursorX = 0, cursorY = 0, cursorZ = 0;  // wartosci przypisywane w konstruktorze,
                                                        // chyba ze wywoływane z konsoli to domyslnie 0

    public ScriptRun(Interpreter interpreter, Path file, Object metadata) {
        this.interpreter = interpreter;
        this.metadata = metadata;
        this.scriptLocation = file;

        if (metadata instanceof net.minecraft.server.command.ServerCommandSource source) {
            var pos = source.getPosition();  // pozycja gracza bo to on wpisuje komende
            this.cursorX = (int) pos.x;
            this.cursorY = (int) pos.y;
            this.cursorZ = (int) pos.z - 1;
        }
    }

    // getter i setter dla wirtualnego kursora
    public void setCursor(int x, int y, int z) {
        this.cursorX = x; this.cursorY = y; this.cursorZ = z;
    }
    public void moveCursor(int x, int y, int z) {
        this.cursorX += x; this.cursorY += y; this.cursorZ += z;
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
        CharStream charStream;
        try {
            charStream = CharStreams.fromPath(scriptLocation);
        } catch (IOException e) {
            interpreter.getLogger().systemError(e, "Failed to read the script even though it's readable?!");
            return false;
        }

        ArchitLexer lexer = new ArchitLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ArchitParser parser = new ArchitParser(tokens);

        // usuwamy domyślne ErrorListenery i dodajemy nasze dla parsera i lexera
        parser.removeErrorListeners();
        parser.addErrorListener(new ScriptErrorListener(this));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ScriptErrorListener(this));

        try {
            ArchitParser.ProgramContext tree = parser.program();
            // stworzenie visitora i uruchomienie
            var typeChecker = new TypeCheckingVisitor(this);
            typeChecker.visit(tree);
        } catch (ScriptException e) {
            return false;
        } catch (RuntimeException e) {
            interpreter.getLogger().systemError(e, "Unknown visitor exception caught!");
            interpreter.getLogger().scriptError(this, "Unknown exception: {}", e.getMessage());
            return false;
        }

        return true;
    }
}
