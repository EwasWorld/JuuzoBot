package CoreBox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;



public class Logger {
    private static final Path mainLogFileLocation = Paths.get(Bot.mainFilePath + "CoreBox/Log.txt");
    private static final Path outputLogFileLocation = Paths.get(Bot.mainFilePath + "CoreBox/LogReport.json");
    private static final Charset charset = StandardCharsets.UTF_8;


    private static boolean init() throws IOException {
        final File file = new File(mainLogFileLocation.toString());
        if (!file.exists()) {
            file.createNewFile();
            return true;
        }
        else {
            return false;
        }
    }


    // TODO Better if this is called when a custom exception is thrown
    public static void logEvent(String command, Exception e) {
        try {
            final boolean isFirstLog = init();

            final List<String> lines = new ArrayList<>();
            if (!isFirstLog) {
                lines.add(",\n");
            }
            lines.add("{");
            lines.add(String.format("\"command\": \"%s\",", command));
            lines.add(String.format("\"message\": \"%s\",", e.getMessage()));
            lines.add(String.format("\"cause\": \"%s\",", e.getCause()));
            lines.add("\"stackTrace\": \"");

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            lines.add(sw.toString());
            pw.close();
            sw.close();

            lines.add("\"\n}");

            appendToFile(mainLogFileLocation, lines);
        } catch (IOException e1) {
            e.printStackTrace();
        }
    }


    public static File getLoggedEventsToSend() {
        try {
            final List<String> lines = new ArrayList<>();
            lines.add("{\n\"log\": [\n");
            lines.addAll(Files.readAllLines(mainLogFileLocation, charset));
            lines.add("\n]\n}");

            appendToFile(outputLogFileLocation, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(outputLogFileLocation.toString());
    }


    private static void appendToFile(Path outFile, List<String> lines) {
        try {
            Files.write(outFile, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void clearLog() {
        new File(mainLogFileLocation.toString()).delete();
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
