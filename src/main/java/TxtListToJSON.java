import CoreBox.Bot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;



public class TxtListToJSON {
    private static final String fileNameNoExtention = Bot.mainFilePath + "CharacterBox/BroadInfo/Trinkets";


    public static void main(String[] args) {
        Scanner scanner;
        PrintStream printStream;
        try {
            scanner = new Scanner(new File(fileNameNoExtention + ".txt"));
            final File outFile = new File(fileNameNoExtention + ".json");
            outFile.createNewFile();
            printStream = new PrintStream(outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String line;
        printStream.println("{");
        printStream.println("\"effects\": [");
        if (scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.replaceAll("\"", "'");
            printToStream(printStream, line);
        }
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.replaceAll("\"", "'");
            if (line.charAt(line.length() - 1) != ".".charAt(0)) {
                line += ".";
            }
            printToStreamWithPrecedingComma(printStream, line);
        }
        printStream.println("]");
        printStream.println("}");
    }


    private static void printToStream(PrintStream ps, String toPrint) {
        ps.print("\"");
        ps.print(toPrint);
        ps.println("\"");
    }


    private static void printToStreamWithPrecedingComma(PrintStream ps, String toPrint) {
        ps.print(", ");
        printToStream(ps, toPrint);
    }
}
