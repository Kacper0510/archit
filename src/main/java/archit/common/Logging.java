package archit.common;

import java.util.ArrayList;
import java.util.List;

public interface Logging {
    void systemInfo(String format, Object... objects);
    void systemError(String format, Object... objects);
    void systemError(Throwable t, String format, Object... objects);
    void scriptPrint(ScriptRun run, String text);
    void scriptError(ScriptRun run, String format, Object... objects);

    default List<String> parseFormatAndSubstitute(String format, Object... objects) {
        var split = (format + " ").split("\\{\\}");
        if (split.length - 1 != objects.length) {
            throw new IllegalArgumentException("Number of arguments does not match number of placeholders");
        }
        var result = new ArrayList<String>();
        result.add(split[0]);
        for (int i = 0; i < objects.length; i++) {
            result.add(objects[i] == null ? "null" : objects[i].toString());
            result.add(split[i + 1]);
        }
        return result;
    }
}
