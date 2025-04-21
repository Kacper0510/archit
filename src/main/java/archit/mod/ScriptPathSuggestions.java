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
    private final ArchitMod archit;

    public ScriptPathSuggestions(ArchitMod archit) {
        this.archit = archit;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<ServerCommandSource> context, SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        try {
            var files = Files.walk(archit.scriptDirectory, 1)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString);
            return CommandSource.suggestMatching(files, builder);
        } catch (IOException e) {
            archit.interpreter.getLogger().systemError(e, "Could not generate script path suggestions!");
        }
        return Suggestions.empty();
    }
}
