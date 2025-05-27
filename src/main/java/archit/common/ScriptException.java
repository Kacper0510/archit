package archit.common;

import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;

public class ScriptException extends RuntimeException {
    private final String message;

    public ScriptException(ScriptRun run, Type type, int line, int col, String format, Object... params) {
        var logger = run.getInterpreter().getLogger();
        format = "{} at line {}, col {}: " + format;
        Object[] newParams = new Object[3 + params.length];
        newParams[0] = type.getMessage();
        newParams[1] = line;
        newParams[2] = col;
        System.arraycopy(params, 0, newParams, 3, params.length);
        message = logger.parseFormatAndSubstitute(format, newParams).stream().collect(Collectors.joining());
        logger.scriptError(run, format, newParams);
    }

    public ScriptException(ScriptRun run, Type type, ParserRuleContext ctx, String format, Object... params) {
        this(run, type, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), format, params);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public enum Type {
        SYNTAX_ERROR("Syntax error"),
        RUNTIME_ERROR("Runtime error"),
        TYPE_ERROR("Type error"),
        NAME_ERROR("Name error"),
        DEAD_CODE("Dead code"),
        LOGIC_ERROR("Logic error");

        private final String message;

        Type(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
