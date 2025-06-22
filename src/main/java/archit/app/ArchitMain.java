package archit.app;

import archit.common.Interpreter;
import archit.common.Material;
import archit.common.ScriptRun;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
        Map<BlockPosition, Material> map = new HashMap<>();
        PlatformNatives platform = new PlatformNatives();
        interpreter.getStandardLibrary().registerNatives(platform);

        this.run = new ScriptRun(interpreter, run.getScriptLocation(), map, run.getArgs());

        interpreter.getCurrentRuns().add(run);
        boolean success = run.run();
        interpreter.getCurrentRuns().remove(run);

        if (success) {
            exportToObj();
        }
    }

    @SuppressWarnings("unchecked")
    public void exportToObj() {
        String scriptFileName = run.getScriptLocation().getFileName().toString();
        String directoryPath = "obj";
        String objFileName = directoryPath + "/" + scriptFileName.replaceFirst("\\.\\w+$", "") + ".obj";
        try {
            // tworzenie folderu
            Files.createDirectories(Paths.get(directoryPath));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(objFileName))) {
                long vertexIndex = 1;

                Map<BlockPosition, Material> map = (Map<BlockPosition, Material>) run.getMetadata();

                for (BlockPosition pos : map.keySet()) {
                    float x = pos.x();
                    float y = pos.y();
                    float z = pos.z();

                    if (!map.getOrDefault(pos, new Material("air")).toString().equals("minecraft:air")) {
                        // dodanie 8 wierzcholkow szescianu
                        for (float[] offset : cubeVertices) {
                            writer.write(
                                String.format("v %.1f %.1f %.1f%n", x + offset[0], y + offset[1], z + offset[2])
                            );
                        }

                        // dodanie 12 scian (12 trojkatow czyli 1 szescian)
                        for (long[] face : cubeFaces) {
                            writer.write(String.format(
                                "f %d %d %d%n",
                                vertexIndex + face[0] - 1,
                                vertexIndex + face[1] - 1,
                                vertexIndex + face[2] - 1
                            ));
                        }
                    }

                    vertexIndex += 8;
                }
            }
        } catch (IOException e) {
            interpreter.getLogger().scriptError(run, "Failed to export to OBJ: {}", e.getMessage());
        }
    }

    // pozycje wierzcholkow szescianu
    private static final float[][] cubeVertices = {
        { 0, 0, 0 }, { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 },  // dolna warstwa
        { 0, 0, 1 }, { 1, 0, 1 }, { 1, 1, 1 }, { 0, 1, 1 }  // górna warstwa
    };

    // wierzcholki 12 trojkatow - wartosci odpowiadaja wierzchołkom szescianu (np. 1 to {0,0,0})
    private static final long[][] cubeFaces = {
        { 1, 2, 3 }, { 1, 3, 4 },  // dol
        { 5, 8, 7 }, { 5, 7, 6 },  // gora
        { 1, 5, 6 }, { 1, 6, 2 },  // przod
        { 2, 6, 7 }, { 2, 7, 3 },  // prawo
        { 3, 7, 8 }, { 3, 8, 4 },  // tyl
        { 4, 8, 5 }, { 4, 5, 1 }  // lewo
    };

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        String script = args.length > 0 ? args[0] : null;
        String joinedArgs = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";

        new ArchitMain(script, joinedArgs).run();
        AnsiConsole.systemUninstall();
    }
}
