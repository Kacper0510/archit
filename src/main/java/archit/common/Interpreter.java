package archit.common;

import archit.common.stdlib.StandardLibrary;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class Interpreter {
    private final List<ScriptRun> currentRuns = new ArrayList<>();
    private final StandardLibrary standardLibrary;
    private final Logging logger;

    public Interpreter(Logging logger) {
        this.logger = logger;
        this.standardLibrary = new StandardLibrary(logger);
    }

    public List<ScriptRun> getCurrentRuns() {
        return currentRuns;
    }

    public Logging getLogger() {
        return logger;
    }

    public StandardLibrary getStandardLibrary() {
        return standardLibrary;
    }

    // logika dla printa
    public void builtinPrint(ScriptRun run, String message) {
        logger.scriptPrint(run, message);
    }

    // logika dla move
    public void builtinMove(ScriptRun run, int x, int y, int z) {
        // ustawiamy stan kursora w run
        run.moveCursor(x, y, z);
        // logger.systemInfo("cursor moved on: {},{},{}", x, y, z);
    }

    // logika dla place
    public void builtinPlace(ScriptRun run, String material) {
        Object meta = run.getMetadata();
        if (meta instanceof ServerCommandSource src) {
            ServerWorld world = src.getWorld();
            Identifier id = Identifier.of(material);
            if (Registries.BLOCK.containsId(id)) {
                Block block = Registries.BLOCK.get(id);
                world.setBlockState(
                    new BlockPos(run.getCursorX(), run.getCursorY(), run.getCursorZ()), block.getDefaultState()
                );
            } else {
                logger.scriptError(run, "Unknown block: {}", material);
            }
        } else {
            logger.systemInfo("PLACE {} at {},{},{}", material, run.getCursorX(), run.getCursorY(), run.getCursorZ());
        }
    }
}
