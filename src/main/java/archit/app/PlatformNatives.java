package archit.app;

import archit.common.Material;
import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlatformNatives {

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

    public void exportToObj(ScriptRun run) {
        String scriptFileName = run.getScriptLocation().getFileName().toString();
        String directoryPath = "obj";
        String objFileName = directoryPath + "/" + scriptFileName.replaceFirst("\\.\\w+$", "") + ".obj";
        try {
            //tworzenie folderu
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(directoryPath));

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(objFileName))) {
                long vertexIndex = 1;

                Map<BlockPosition, Material> map = (Map<BlockPosition, Material>) run.getMetadata();

                for (BlockPosition pos : map.keySet()) {
                    float x = pos.x();
                    float y = pos.y();
                    float z = pos.z();

                    if(!map.getOrDefault(pos, new Material("air")).toString().equals("minecraft:air")) {

                        //dodanie 8 wierzcholkow szescianu
                        for (float[] offset : cubeVertices) {
                            writer.write(String.format("v %.1f %.1f %.1f\n", x + offset[0], y + offset[1], z + offset[2]));
                        }

                        //dodanie 12 scian (12 trojkatow czyli 1 szescian)
                        for (long[] face : cubeFaces) {
                            writer.write(String.format("f %d %d %d\n",
                                    vertexIndex + face[0] - 1,
                                    vertexIndex + face[1] - 1,
                                    vertexIndex + face[2] - 1
                            ));
                        }
                    }

                    vertexIndex += 8;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export OBJ", e);    //jaki wyjątek???????
        }
    }

    //pozycje wierzcholkow szescianu
    private static final float[][] cubeVertices = {
            {0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0},  //dolna warstwa
            {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}   //górna warstwa
    };

    //wierzcholki 12 trojkatow - wartosci odpowiadaja wierzchołkom szescianu (np. 1 to {0,0,0})
    private static final long[][] cubeFaces = {
            {1, 2, 3}, {1, 3, 4}, //dol
            {5, 8, 7}, {5, 7, 6}, //gora
            {1, 5, 6}, {1, 6, 2}, //przod
            {2, 6, 7}, {2, 7, 3}, //prawo
            {3, 7, 8}, {3, 8, 4}, //tyl
            {4, 8, 5}, {4, 5, 1}  //lewo
    };
}
