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

    @ArchitNative("native abs(value: real): real;")
    public Double abs(ScriptRun run, Double value) {
        return Math.abs(value);
    }

    @ArchitNative("native abs(value: number): number;")
    public BigInteger abs(ScriptRun run, BigInteger value) {
        return value.abs();
    }

    @ArchitNative("native floor(value: real): number;")
    public BigInteger floor(ScriptRun run, Double value) {
        return BigInteger.valueOf((long) Math.floor(value));
    }

    @ArchitNative("native ceil(value: real): number;")
    public BigInteger ceil(ScriptRun run, Double value) {
        return BigInteger.valueOf((long) Math.ceil(value));
    }

    @ArchitNative("native round(value: real): number;")
    public BigInteger round(ScriptRun run, Double value) {
        return BigInteger.valueOf(Math.round(value));
    }
}
