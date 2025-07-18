package archit.common.natives;

import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;

import java.math.BigInteger;

public class Randomness {

    @ArchitNative("native random(min: real, max: real): real;")
    public Double random(ScriptRun run, Double min, Double max) {
        if (max <= min) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return run.getRandom().nextDouble(min, max);
    }

    @ArchitNative("native random(min: number, max: number): number;")
    public BigInteger random(ScriptRun run, BigInteger min, BigInteger max) {
        if (max.compareTo(min) <= 0) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return BigInteger.valueOf(run.getRandom().nextLong(min.longValueExact(),max.longValueExact()));
    }

    @ArchitNative("native seed(value: number);")
    public void seed(ScriptRun run, BigInteger value) {run.getRandom().setSeed(value.longValue());}
}
