package archit.app;

import archit.common.Material;
import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;

// TODO: implement 3D objects
public class PlatformNatives {
    @ArchitNative("native place(block: material);")
    public void place(ScriptRun run, Material block) {
        run.getInterpreter().getLogger().systemInfo(
            "PLACE {} at {}, {}, {}", block, run.getCursorX(), run.getCursorY(), run.getCursorZ()
        );
    }

    @ArchitNative("native check(): material;")
    public Material check(ScriptRun run) {
        run.getInterpreter().getLogger().systemInfo(
            "CHECK at {}, {}, {}", run.getCursorX(), run.getCursorY(), run.getCursorZ()
        );
        return new Material("air");
    }
}
