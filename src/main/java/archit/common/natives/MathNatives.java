package archit.common.natives;

import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;
import java.math.BigInteger;

public class MathNatives {
    @ArchitNative("native sqrt(value: real): real;")
    public Double sqrt(ScriptRun run, Double value) {
        return Math.sqrt(value);
    }

    @ArchitNative("native sqrt(value: number): real;")
    public Double sqrt(ScriptRun run, BigInteger value) {
        return Math.sqrt(value.doubleValue());
    }
}
