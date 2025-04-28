package archit.common.natives;

import java.math.BigInteger;

import archit.common.stdlib.ArchitNative;

public class MathNatives {
    @ArchitNative("native sqrt(value: real): real;")
    public Double sqrt(Double value) {
        return Math.sqrt(value);
    }

    @ArchitNative("native sqrt(value: number): real;")
    public Double sqrt(BigInteger value) {
        return Math.sqrt(value.doubleValue());
    }
}
