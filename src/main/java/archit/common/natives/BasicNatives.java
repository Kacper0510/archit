package archit.common.natives;

import archit.common.ArchitFunction;
import archit.common.ScriptRun;
import archit.common.Type;
import archit.common.stdlib.ArchitDynamic;
import archit.common.stdlib.ArchitNative;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class BasicNatives {
    @ArchitNative("native __old_print(message: string);")
    public void print(ScriptRun run, String message) {
        run.getInterpreter().getLogger().scriptPrint(run, message);
    }

    @ArchitDynamic
    public Optional<ArchitFunction> print(Type[] types) {
        if (types.length == 1) {
            return Optional.of(ArchitFunction.fromFunction("print", null, types, (run, p) -> {
                run.getInterpreter().getLogger().scriptPrint(run, types[0].toStringObject(p[0]));
                return null;
            }, "object"));
        }
        return Optional.empty();
    }

    @ArchitNative("native move(direction: <posx, negx, posy, negy, posz, negz>);")
    public void move(ScriptRun run, String direction) {
        switch (direction) {
            case "posx" -> run.moveCursor(1, 0, 0);
            case "negx" -> run.moveCursor(-1, 0, 0);
            case "posy" -> run.moveCursor(0, 1, 0);
            case "negy" -> run.moveCursor(0, -1, 0);
            case "posz" -> run.moveCursor(0, 0, 1);
            case "negz" -> run.moveCursor(0, 0, -1);
            default -> throw new IllegalStateException("Invalid move direction.");
        }
    }

    @ArchitNative("native move(x: number, y: number, z: number);")
    public void move(ScriptRun run, BigInteger x, BigInteger y, BigInteger z) {
        run.moveCursor(x.intValue(), y.intValue(), z.intValue());
    }

    @ArchitNative("native move(vector: [number]);")
    public void move(ScriptRun run, List<BigInteger> vector) {
        run.moveCursor(vector.get(0).intValue(), vector.get(1).intValue(), vector.get(2).intValue());
    }

    @ArchitNative("native position(): [number];")
    public List<BigInteger> position(ScriptRun run) {
        return List.of(
            BigInteger.valueOf(run.getCursorX()),
            BigInteger.valueOf(run.getCursorY()),
            BigInteger.valueOf(run.getCursorZ())
        );
    }

    @ArchitNative("native args(): string;")
    public String args(ScriptRun run) {return run.getArgs();}
}
