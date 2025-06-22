package archit.app;

import archit.common.Interpreter;
import archit.common.ScriptRun;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import org.fusesource.jansi.AnsiConsole;

public class ArchitMain {
    private Interpreter interpreter;
    private ScriptRun run;

    public ArchitMain(String script, String argsString) {
        var logging = new LoggingImpl(new File("log.txt"));
        this.interpreter = new Interpreter(logging);
        if (script == null) {
            logging.scriptError(null, "HINT: Pass the script name as a CLI argument.");
            script = ".";
        }
        this.run = new ScriptRun(interpreter, Path.of(script), null, argsString);
    }

    public void run() {
        PlatformNatives platform = new PlatformNatives();
        interpreter.getStandardLibrary().registerNatives(platform);
        interpreter.getCurrentRuns().add(run);
        run.run();
        interpreter.getCurrentRuns().remove(run);

        //eksportowanie do .obj
        var scriptPath = run.getScriptLocation();
        platform.exportToObj(scriptPath.getFileName().toString());
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        String script = args.length > 0 ? args[0] : null;
        String joinedArgs = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";

        new ArchitMain(script, joinedArgs).run();
        AnsiConsole.systemUninstall();
    }
}
