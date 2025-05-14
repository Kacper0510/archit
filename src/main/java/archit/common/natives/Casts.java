package archit.common.natives;

import archit.common.Material;
import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;
import java.math.BigInteger;

public class Casts {
    @ArchitNative("native as_real(value: number): real;")
    public Double asReal(ScriptRun run, BigInteger value) {
        return value.doubleValue();
    }

    @ArchitNative("native as_real(value: string): real;")
    public Double asReal(ScriptRun run, String value) {
        return Double.parseDouble(value.replace("_", ""));
    }

    @ArchitNative("native as_number(value: real): number;")
    public BigInteger asNumber(ScriptRun run, Double value) {
        return BigInteger.valueOf(value.longValue());
    }

    @ArchitNative("native as_number(value: string): number;")
    public BigInteger asNumber(ScriptRun run, String value) {
        return new BigInteger(value.replace("_", ""));
    }

    @ArchitNative("native as_material(value: string): material;")
    public Material asMaterial(ScriptRun run, String value) {
        if (value.startsWith(":")) {
            return new Material(value.substring(1));
        }
        var split = value.split(":");
        if (split.length == 1) {
            return new Material(split[0]);
        } else {
            return new Material(split[0], split[1]);
        }
    }
}
