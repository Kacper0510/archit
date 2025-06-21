package archit.mod;

import archit.common.ScriptRun;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public class RunIdSuggestions implements SuggestionProvider<ServerCommandSource> {
    private final ArchitMod archit;

    public RunIdSuggestions(ArchitMod archit) {
        this.archit = archit;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<ServerCommandSource> context, SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        var runs = archit.interpreter.getCurrentRuns().stream().map(ScriptRun::toString);
        return CommandSource.suggestMatching(runs, builder);
    }
}
