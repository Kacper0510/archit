package archit.common;

import org.antlr.v4.runtime.*;

public class ScriptErrorListener extends BaseErrorListener {
    private final ScriptRun run;
    private final Logging logger;

    public ScriptErrorListener(ScriptRun run, Logging logger) {
        this.run = run;
        this.logger = logger;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
    ) {
        logger.scriptError(run, "Syntax error at line {}, char {}: {}", line, charPositionInLine, msg);
        throw new RuntimeException("Script aborted due to syntax error.");
    }
}
