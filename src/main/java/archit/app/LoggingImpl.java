package archit.app;

import archit.common.Logging;
import archit.common.ScriptRun;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.stream.Collectors;
import org.fusesource.jansi.Ansi.Color;

import static org.fusesource.jansi.Ansi.*;

public class LoggingImpl implements Logging {
    private final BufferedWriter logger;
    private static final Color INFO_COLOR = Color.CYAN;
    private static final Color ERROR_COLOR = Color.RED;
    private static final Color PARAM_COLOR = Color.YELLOW;

    public LoggingImpl(File logFile) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            writer = new BufferedWriter(Writer.nullWriter());
        }
        logger = writer;
    }

    @Override
    public void systemInfo(String format, Object... objects) {
        var text = parseFormatAndSubstitute(format, objects).stream().collect(Collectors.joining());
        try {
            logger.append("[INFO] ").append(text).append('\n');
            logger.flush();
        } catch (IOException e) {
            // No-op
        }
    }

    @Override
    public void systemError(String format, Object... objects) {
        var text = parseFormatAndSubstitute(format, objects).stream().collect(Collectors.joining());
        try {
            logger.append("[ERROR] ").append(text).append('\n');
            logger.flush();
        } catch (IOException e) {
            // No-op
        }
    }

    @Override
    public void systemError(Throwable t, String format, Object... objects) {
        var text = parseFormatAndSubstitute(format, objects).stream().collect(Collectors.joining());
        try {
            logger.append("[ERROR] ").append(text).append('\n');
            var errorRepr = new StringWriter();
            t.printStackTrace(new PrintWriter(errorRepr));
            logger.append(errorRepr.toString()).append('\n');
            logger.flush();
        } catch (IOException e) {
            // No-op
        }
    }

    @Override
    public void scriptPrint(ScriptRun run, String text) {
        try {
            logger.append("[STDOUT] > ").append(text).append('\n');
            logger.flush();
        } catch (IOException e) {
            // No-op
        }
        System.out.print(ansi().fg(INFO_COLOR).a("> ").a(text).a('\n').reset());
    }

    @Override
    public void scriptError(ScriptRun run, String format, Object... objects) {
        var strings = parseFormatAndSubstitute(format, objects);
        try {
            logger.append("[STDERR] ").append(strings.stream().collect(Collectors.joining())).append('\n');
            logger.flush();
        } catch (IOException e) {
            // No-op
        }
        for (int i = 0; i < strings.size(); i++) {
            var color = (i % 2 == 0) ? ERROR_COLOR : PARAM_COLOR;
            System.out.print(ansi().fg(color).a(strings.get(i)));
        }
        System.out.print(ansi().a('\n').reset());
    }
}
