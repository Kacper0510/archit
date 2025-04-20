package archit.app;

import archit.common.Utils;
import org.fusesource.jansi.AnsiConsole;

public class ArchitMain {
    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        System.out.println(Utils.test());
        AnsiConsole.systemUninstall();
    }
}
