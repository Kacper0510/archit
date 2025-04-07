package archit.mod;

import archit.common.Utils;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchitMod implements ModInitializer {
    public static final String MOD_ID = "archit";
    public static final Logger LOGGER = LoggerFactory.getLogger("archit");

    public static Path scriptDirectory;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info(Utils.test());

        scriptDirectory = FabricLoader.getInstance().getGameDir().resolve("archit-scripts");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("archit").then(CommandManager.literal("run").then(
                CommandManager.argument("name", StringArgumentType.string())
                    .suggests(new ScriptPathSuggestions())
                    .executes(context -> runScript(context.getSource(), StringArgumentType.getString(context, "name")))
            )));
        });
    }

    private int runScript(ServerCommandSource source, String scriptName) {
        Path scriptPath = scriptDirectory.resolve(scriptName);

        if (!Files.exists(scriptPath) || !Files.isRegularFile(scriptPath)) {
            source.sendFeedback(() -> Text.literal("File does not exist: " + scriptPath), false);
            return 0;
        }

        try {
            String content = Files.readString(scriptPath);
            source.sendFeedback(() -> Text.literal("Content: " + scriptName + ":\n" + content), false);
        } catch (IOException e) {
            source.sendFeedback(() -> Text.literal("Failed to load the file: " + e.getMessage()), false);
        }

        return ControlFlowAware.Command.SINGLE_SUCCESS;
    }
}
