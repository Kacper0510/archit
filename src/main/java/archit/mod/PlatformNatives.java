package archit.mod;

import archit.common.Material;
import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PlatformNatives {
    @ArchitNative("native place(block: material);")
    public void place(ScriptRun run, Material block) {
        var src = (ServerCommandSource) run.getMetadata();
        ServerWorld world = src.getWorld();
        Identifier id = Identifier.of(block.toString());
        if (!Registries.BLOCK.containsId(id)) {
            throw new IllegalArgumentException("Unknown block " + block);
        }
        Block mcBlock = Registries.BLOCK.get(id);
        world.setBlockState(
            new BlockPos(run.getCursorX(), run.getCursorY(), run.getCursorZ()), mcBlock.getDefaultState()
        );
    }

    @ArchitNative("native check(): material;")
    public Material check(ScriptRun run) {
        var src = (ServerCommandSource) run.getMetadata();
        ServerWorld world = src.getWorld();
        Block block = world.getBlockState(new BlockPos(run.getCursorX(), run.getCursorY(), run.getCursorZ())).getBlock();
        var id = Registries.BLOCK.getId(block);
        return new Material(id.getNamespace(), id.getPath());
    }
}
