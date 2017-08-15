package Grog;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class GrogTxtToJSON {
    public static void main(String[] args) {
        Scanner scanner = null;
        File file;
        PrintStream printStream = null;
        try {
            scanner = new Scanner(new File("src/Grog/GrogEffects.txt"));
            file = new File("src/Grog/GrogEffects.json");
            file.createNewFile();
            printStream = new PrintStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line;
        printStream.println("{");
        printStream.println("\"effects\": [");
        if (scanner != null && scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.replaceAll("\"", "'");
            printToStream(printStream, line);
        }
        while (scanner != null && scanner.hasNext()) {
            line = scanner.nextLine();
            line = line.replaceAll("\"", "'");
            printToStreamWithComma(printStream, line);
        }
        printStream.println("]");
        printStream.println("}");
    }

    private static void printToStream(PrintStream ps, String toPrint) {
        ps.print("\"");
        ps.print(toPrint);
        ps.println("\"");
    }

    private static void printToStreamWithComma(PrintStream ps, String toPrint) {
        ps.print(", ");
        printToStream(ps, toPrint);
    }
}
