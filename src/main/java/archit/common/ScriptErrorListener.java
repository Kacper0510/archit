package archit.common;

import org.antlr.v4.runtime.*;

public class ScriptErrorListener extends BaseErrorListener {
    private final ScriptRun run;

    public ScriptErrorListener(ScriptRun run) {
        this.run = run;
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
        if (run == null) {
            throw e;
        }
        throw new ScriptException(run, ScriptException.Type.SYNTAX_ERROR, line, charPositionInLine, msg);
    }
}
