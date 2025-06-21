package archit.mod;

import archit.common.Interpreter;
import archit.common.ScriptRun;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ArchitMod implements ModInitializer {
    public static final String MOD_ID = "archit";

    public Path scriptDirectory;
    public Interpreter interpreter;

    @Override
    public void onInitialize() {
        interpreter = new Interpreter(new LoggingImpl());
        interpreter.getStandardLibrary().registerNatives(new PlatformNatives());

        scriptDirectory = FabricLoader.getInstance().getGameDir().resolve("archit-scripts");
        try {
            Files.createDirectories(scriptDirectory);
        } catch (IOException e) {
            interpreter.getLogger().systemError(e, "Failed to create archit-scripts directory!");
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("archit")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.literal("run").then(
                        CommandManager.argument("script_name", StringArgumentType.string())
                            .suggests(new ScriptPathSuggestions(this))
                            .executes(
                                context
                                -> runScript(context.getSource(), StringArgumentType.getString(context, "script_name"))
                            )
                    ))
                    .then(CommandManager.literal("stop").then(
                        CommandManager.argument("run_id", StringArgumentType.greedyString())
                            .suggests(new RunIdSuggestions(this))
                            .executes(context -> stopScript(StringArgumentType.getString(context, "run_id")))
                    ))
                    .then(CommandManager.literal("animate").then(
                        CommandManager.argument("period", IntegerArgumentType.integer(1, 100))
                            .then(CommandManager.argument("script_name", StringArgumentType.string())
                                .suggests(new ScriptPathSuggestions(this))
                                .executes(
                                    context
                                    -> runScript(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "script_name"),
                                        IntegerArgumentType.getInteger(context, "period")
                                    )
                                ))
                    ))
            );
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> onEachTick());
    }

    private int runScript(ServerCommandSource source, String scriptName) {
        Path scriptPath = scriptDirectory.resolve(scriptName);
        var run = new ScriptRun(interpreter, scriptPath, source);
        var pos = source.getPosition();
        run.setCursor((int) pos.x, (int) pos.y, (int) pos.z);
        boolean success = run.startExecution();
        return success ? 1 : 0;
    }

    private int runScript(ServerCommandSource source, String scriptName, int animationSpeed) {
        Path scriptPath = scriptDirectory.resolve(scriptName);
        var run = new ScriptRun(interpreter, scriptPath, source, animationSpeed);
        var pos = source.getPosition();
        run.setCursor((int) pos.x, (int) pos.y, (int) pos.z);
        boolean success = run.startExecution();
        return success ? 1 : 0;
    }

    private int stopScript(String runId) {
        var runs = new ArrayList<>(interpreter.getCurrentRuns());
        for (var run : runs) {
            if (run.toString().equals(runId)) {
                run.stopExecution();
            }
        }
        return runs.size() - interpreter.getCurrentRuns().size();
    }

    private void onEachTick() {
        // to avoid ConcurrentModificationException
        var runs = new ArrayList<>(interpreter.getCurrentRuns());
        for (var run : runs) {
            run.runNextTick();
        }
    }
}
