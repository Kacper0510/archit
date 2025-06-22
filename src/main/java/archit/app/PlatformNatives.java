package archit.app;

import archit.common.Material;
import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;
import java.util.Map;

public class PlatformNatives {
    @SuppressWarnings("unchecked")
    private Map<BlockPosition, Material> getMap(ScriptRun run) {
        return (Map<BlockPosition, Material>) run.getMetadata();
    }

    @ArchitNative("native place(block: material);")
    public void place(ScriptRun run, Material block) {
        var pos = new BlockPosition(run.getCursorX(), run.getCursorY(), run.getCursorZ());

        getMap(run).put(pos, block);

        run.getInterpreter().getLogger().systemInfo(
                "PLACE {} at {}, {}, {}", block, pos.x(), pos.y(), pos.z()
        );
    }

    @ArchitNative("native check(): material;")
    public Material check(ScriptRun run) {
        run.getInterpreter().getLogger().systemInfo(
                "CHECK at {}, {}, {}", run.getCursorX(), run.getCursorY(), run.getCursorZ()
        );

        var pos = new BlockPosition(
                run.getCursorX(), run.getCursorY(), run.getCursorZ()
        );

        return getMap(run).getOrDefault(pos, new Material("air"));
    }
}
