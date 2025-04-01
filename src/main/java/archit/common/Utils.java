package archit.common;

import archit.parser.ArchitLexer;
import org.antlr.v4.runtime.CharStreams;

public class Utils {
    private Utils() {
        // Private constructor to prevent instantiation
    }

    public static String test() {  // TODO remove
        var lexer = new ArchitLexer(CharStreams.fromString("move $posx;"));
        return "Hello, " + lexer.getGrammarFileName();
    }
}
