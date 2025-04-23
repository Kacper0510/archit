package archit.common;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.block.Block;

public class Interpreter {
    private final List<ScriptRun> currentRuns = new ArrayList<>();
    private final Logging logger;
    
    public Interpreter(Logging logger) {
        this.logger = logger;
    }

    public List<ScriptRun> getCurrentRuns() {
        return currentRuns;
    }

    public Logging getLogger() {
        return logger;
    }

    // logika dla printa
    public void builtinPrint(ScriptRun run, String message) {
        logger.scriptPrint(run, message);
    }

    // logika dla move
    public void builtinMove(ScriptRun run, int x, int y, int z) {
        // ustawiamy stan kursora w run
        run.setCursor(x, y, z);

        logger.systemInfo("cursor moved on: {},{},{}", x, y, z);
    }

    //logika dla place
    public void builtinPlace(ScriptRun run, String material) {
        Object meta = run.getMetadata();
        if (meta instanceof ServerCommandSource src) {
            ServerWorld world = src.getWorld();
            Identifier id = Identifier.of(material);
            if (Registries.BLOCK.containsId(id)) {
                Block block = Registries.BLOCK.get(id);
                world.setBlockState(new BlockPos(run.getCursorX(), run.getCursorY(), run.getCursorZ()), block.getDefaultState());
            } else {
                logger.scriptError(run, "Unknown block: {}", material);
            }
        } else {
            logger.systemInfo("PLACE {} at {},{},{}", material, run.getCursorX(), run.getCursorY(), run.getCursorZ());
        }
    }
}
