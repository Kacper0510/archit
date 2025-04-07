package archit.mod;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public class ScriptPathSuggestions implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<ServerCommandSource> context, SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        try {
            var files = Files.walk(ArchitMod.scriptDirectory, 1)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString);
            return CommandSource.suggestMatching(files, builder);
        } catch (IOException e) {
            ArchitMod.LOGGER.error("Could not generate script path suggestions!", e);
        }
        return Suggestions.empty();
    }
}
