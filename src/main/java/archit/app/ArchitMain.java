package archit.app;

import archit.common.Interpreter;
import archit.common.ScriptRun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.fusesource.jansi.AnsiConsole;

public class ArchitMain {
    private Interpreter interpreter;
    private ScriptRun run;

    public ArchitMain(String script) {
        var logging = new LoggingImpl(new File("log.txt"));
        this.interpreter = new Interpreter(logging);
        if (script == null) {
            logging.scriptError(null, "HINT: Pass the script name as a CLI argument.");
            script = ".";
        }
        this.run = new ScriptRun(interpreter, Path.of(script));
    }

    public void run() throws IOException {
        interpreter.getCurrentRuns().add(run);
        run.run();
        interpreter.getCurrentRuns().remove(run);
    }

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        new ArchitMain(args.length > 0 ? args[0] : null).run();
        AnsiConsole.systemUninstall();
    }
}
