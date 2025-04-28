package archit.common;

public class ScriptExceptions {

    public static class SyntaxException extends RuntimeException {
        public SyntaxException(String message) {
            super(message);
        }
    }

    public static class VariableException extends RuntimeException {
        public VariableException(String message) {
            super(message);
        }
    }

    public static class InterpolationException extends RuntimeException {
        public InterpolationException(String message) {
            super(message);
        }
    }

    public static class UnexpectedException extends RuntimeException {
        public UnexpectedException(String message) {
            super(message);
        }
    }
}
