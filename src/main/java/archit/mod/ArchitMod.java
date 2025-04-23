package archit.mod;

import archit.common.Interpreter;
import archit.common.ScriptRun;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
                        CommandManager.argument("name", StringArgumentType.string())
                            .suggests(new ScriptPathSuggestions(this))
                            .executes(
                                context -> runScript(context.getSource(), StringArgumentType.getString(context, "name"))
                            )
                    ))
            );
        });
    }

    private int runScript(ServerCommandSource source, String scriptName) {
        Path scriptPath = scriptDirectory.resolve(scriptName);
        var run = new ScriptRun(interpreter, scriptPath, source);
        interpreter.getCurrentRuns().add(run);
        boolean success = run.run();
        interpreter.getCurrentRuns().remove(run);
        return success ? 1 : 0;
    }
}
