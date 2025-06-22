package archit.mod;

import archit.common.Logging;
import archit.common.ScriptRun;
import java.util.stream.Collectors;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingImpl implements Logging {
    private final Logger logger = LoggerFactory.getLogger("archit");
    private static final Formatting INFO_COLOR = Formatting.AQUA;
    private static final Formatting ERROR_COLOR = Formatting.RED;
    private static final Formatting PARAM_COLOR = Formatting.YELLOW;
    private static final String PROMPT = "[archit] ";

    @Override
    public void systemInfo(String format, Object... objects) {
        logger.info(format, objects);
    }

    @Override
    public void systemError(String format, Object... objects) {
        logger.error(format, objects);
    }

    @Override
    public void systemError(Throwable t, String format, Object... objects) {
        logger.error(parseFormatAndSubstitute(format, objects).stream().collect(Collectors.joining()), t);
    }

    @Override
    public void scriptPrint(ScriptRun run, String text) {
        var coloredText = Text.literal(PROMPT + "> " + text).formatted(INFO_COLOR);
        ((ServerCommandSource) run.getMetadata()).sendFeedback(() -> coloredText, false);
    }

    @Override
    public void scriptError(ScriptRun run, String format, Object... objects) {
        MutableText text = Text.literal(PROMPT).formatted(ERROR_COLOR);
        var strings = parseFormatAndSubstitute(format, objects);
        for (int i = 0; i < strings.size(); i++) {
            var color = (i % 2 == 0) ? ERROR_COLOR : PARAM_COLOR;
            text.append(Text.literal(strings.get(i)).formatted(color));
        }
        ((ServerCommandSource) run.getMetadata()).sendFeedback(() -> text, false);
    }

    @Override
    public void scriptDebug(ScriptRun run, String format, Object... objects) {
        MutableText text = Text.literal(PROMPT).formatted(INFO_COLOR);
        var strings = parseFormatAndSubstitute(format, objects);
        for (int i = 0; i < strings.size(); i++) {
            var color = (i % 2 == 0) ? INFO_COLOR : PARAM_COLOR;
            text.append(Text.literal(strings.get(i)).formatted(color));
        }
        var cs = (ServerCommandSource) run.getMetadata();
        if (cs.getPlayer() != null) {
            cs.getPlayer().sendMessage(text, true);
        } else {
            cs.sendFeedback(() -> text, false);
        }
    }
}
