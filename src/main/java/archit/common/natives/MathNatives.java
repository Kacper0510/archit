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

    @ArchitNative("native sin(value: real): real;")
    public Double sin(ScriptRun run, Double value) {return Math.sin(value);}

    @ArchitNative("native cos(value: real): real;")
    public Double cos(ScriptRun run, Double value) {return Math.cos(value);}

    @ArchitNative("native tan(value: real): real;")
    public Double tan(ScriptRun run, Double value) {return Math.tan(value);}

    @ArchitNative("native log(base: real, value: real): real;")
    public Double log(ScriptRun run, Double base, Double value) {return Math.log(value) / Math.log(base);}

    @ArchitNative("native log(base: number, value: number): real;")
    public Double log(ScriptRun run, BigInteger base, BigInteger value) {return Math.log(value.doubleValue()) / Math.log(base.doubleValue());}

    @ArchitNative("native sign(value: real): number;")
    public BigInteger sign(ScriptRun run, Double value) {return BigInteger.valueOf(Long.signum((long) Math.signum(value)));}

    @ArchitNative("native sign(value: number): number;")
    public BigInteger sign(ScriptRun run, BigInteger value) {return BigInteger.valueOf(value.signum());}

    @ArchitNative("native toRadians(deg: real): real;")
    public Double toRadians(ScriptRun run, Double deg) {return Math.toRadians(deg);}

    @ArchitNative("native toDegrees(rad: real): real;")
    public Double toDegrees(ScriptRun run, Double rad) {return Math.toDegrees(rad);}
}
