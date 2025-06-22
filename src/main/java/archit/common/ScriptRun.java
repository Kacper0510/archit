package archit.common;

import archit.common.visitors.EvaluationVisitor;
import archit.common.visitors.TypeCheckingVisitor;
import archit.parser.ArchitLexer;
import archit.parser.ArchitParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class ScriptRun {
    private static final long TICK_LIMIT_NANOS = 3_000_000;
    private static final DateTimeFormatter START_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
    private EvaluationVisitor visitor;
    private final LocalTime startTime = LocalTime.now();
    private Optional<Integer> animationSpeed = Optional.empty();  // animation speed in ticks, if applicable
    private long ticks = -1;

    public ScriptRun(Interpreter interpreter, Path file, Object metadata) {
        this.interpreter = interpreter;
        this.metadata = metadata;
        this.scriptLocation = file;
    }

    public ScriptRun(Interpreter i, Path f, Object m, int animationSpeed) {
        this(i, f, m);
        this.animationSpeed = Optional.of(animationSpeed);
    }

    @Override
    public String toString() {
        return scriptLocation.getFileName().toString() + "@" + START_FORMATTER.format(startTime);
    }

    // getter i setter dla wirtualnego kursora
    public void setCursor(int x, int y, int z) {
        this.cursorX = x;
        this.cursorY = y;
        this.cursorZ = z;
    }

    public void moveCursor(int x, int y, int z) {
        this.cursorX += x;
        this.cursorY += y;
        this.cursorZ += z;
    }

    public int getCursorX() {
        return cursorX;
    }

    public int getCursorY() {
        return cursorY;
    }

    public int getCursorZ() {
        return cursorZ;
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
    public boolean startExecution() {
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
            visitor = new EvaluationVisitor(this, typeChecker.getTables(), tree);
            interpreter.getCurrentRuns().add(this);
            interpreter.getLogger().scriptDebug(this, "Script started: {}", toString());
        } catch (ScriptException e) {
            return false;
        } catch (RuntimeException e) {
            interpreter.getLogger().systemError(e, "Unknown type checking exception caught!");
            interpreter.getLogger().scriptError(this, "Unknown exception: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public void runNextTick() {
        ticks++;
        if (animationSpeed.isPresent() && ticks % animationSpeed.get() != 0) {
            return;
        }

        var start = System.nanoTime();
        do {
            var call = visitor.getNextCall();
            if (call.isEmpty()) {
                stopExecution();
                return;
            } else if (animationSpeed.isPresent()
                       && call.get() instanceof EvaluationVisitor.FunctionCallDebugInfo fcdi) {
                interpreter.getLogger().scriptDebug(this, "Function call: {}", fcdi);
                return;
            }
            try {
                call.get().run();
            } catch (ScriptException e) {
                stopExecution();
                return;
            } catch (RuntimeException e) {
                interpreter.getLogger().systemError(e, "Unknown runtime exception caught!");
                interpreter.getLogger().scriptError(this, "Unknown exception: {}", e.getMessage());
                stopExecution();
                return;
            }
        } while (System.nanoTime() - start < TICK_LIMIT_NANOS);
    }

    public void stopExecution() {
        interpreter.getCurrentRuns().remove(this);
        interpreter.getLogger().scriptDebug(this, "Script stopped: {}", toString());
    }
}
