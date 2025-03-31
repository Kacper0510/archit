package archit;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.ControlFlowAware;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Archit implements ModInitializer {
	public static final String MOD_ID = "archit";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Archit successfully initialized!");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("script")
					.then(CommandManager.literal("run")
							.then(CommandManager.argument("name", StringArgumentType.string())
									.executes(context -> runScript(context.getSource(), StringArgumentType.getString(context, "name")))
							)
					)
			);
		});
	}

	private int runScript(ServerCommandSource source, String scriptName) {
		Path scriptPath = Path.of("C:\\Users\\dawid\\AppData\\Roaming\\.minecraft\\archit-scripts", scriptName);

		if (!Files.exists(scriptPath)) {
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