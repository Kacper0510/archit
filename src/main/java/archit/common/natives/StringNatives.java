package archit.common.natives;

import archit.common.ScriptRun;
import archit.common.stdlib.ArchitNative;

import java.math.BigInteger;

public class StringNatives {

    @ArchitNative("native length(text: string): number;")
    public BigInteger length(ScriptRun run, String text) {return BigInteger.valueOf(text.length());}

    @ArchitNative("native upper(text: string): string;")
    public String toUpperCase(ScriptRun run, String text) {return text.toUpperCase();}

    @ArchitNative("native lower(text: string): string;")
    public String toLowerCase(ScriptRun run, String text) {return text.toLowerCase();}

    @ArchitNative("native contains(text: string, part: string): boolean;")
    public Boolean contains(ScriptRun run, String text, String part) {return text.contains(part);}

    @ArchitNative("native startsWith(text: string, prefix: string): boolean;")
    public Boolean startsWith(ScriptRun run, String text, String prefix) {return text.startsWith(prefix);}

    @ArchitNative("native endsWith(text: string, suffix: string): boolean;")
    public Boolean endsWith(ScriptRun run, String text, String suffix) {return text.endsWith(suffix);}

    @ArchitNative("native indexOf(text: string, part: string): number;")
    public BigInteger indexOf(ScriptRun run, String text, String part) {return BigInteger.valueOf(text.indexOf(part));}

    @ArchitNative("native substring(text: string, begin: number, end: number): string;")
    public String substring(ScriptRun run, String text, BigInteger begin, BigInteger end) {return text.substring(begin.intValue(), end.intValue());}

    @ArchitNative("native replace(text: string, target: string, replacement: string): string;")
    public String replace(ScriptRun run, String text, String target, String replacement) {return text.replace(target, replacement);}

    @ArchitNative("native trim(text: string): string;")
    public String trim(ScriptRun run, String text) {return text.trim();}

    @ArchitNative("native isEmpty(text: string): boolean;")
    public Boolean isEmpty(ScriptRun run, String text) {return text.isEmpty();}

    @ArchitNative("native equals(a: string, b: string): boolean;")
    public Boolean equals(ScriptRun run, String a, String b) {return a.equals(b);}

    @ArchitNative("native equalsIgnoreCase(a: string, b: string): boolean;")
    public Boolean equalsIgnoreCase(ScriptRun run, String a, String b) {return a.equalsIgnoreCase(b);}

    @ArchitNative("native compareTo(a: string, b: string): number;")
    public BigInteger compareTo(ScriptRun run, String a, String b) {return BigInteger.valueOf(a.compareTo(b));}

    @ArchitNative("native matches(text: string, pattern: string): boolean;")
    public Boolean matches(ScriptRun run, String text, String pattern) {return text.matches(pattern);}

}
