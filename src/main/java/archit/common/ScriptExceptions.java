package archit.common;

public abstract class ScriptExceptions extends RuntimeException {
    protected ScriptExceptions(String message) {
        super(message);
    }

    public static class SyntaxException extends ScriptExceptions {
        public SyntaxException(String message) {
            super(message);
        }
    }

    public static class VariableException extends ScriptExceptions {
        public VariableException(String message) {
            super(message);
        }
    }

    public static class InterpolationException extends ScriptExceptions {
        public InterpolationException(String message) {
            super(message);
        }
    }

    public static class UnexpectedException extends ScriptExceptions {
        public UnexpectedException(String message) {
            super(message);
        }
    }
}
