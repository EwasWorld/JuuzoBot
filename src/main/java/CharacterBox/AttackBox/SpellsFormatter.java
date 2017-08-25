package CharacterBox.AttackBox;

import CoreBox.Bot;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Iterator;

import static java.nio.file.Files.readAllBytes;



public class SpellsFormatter {
    public static final String fileInLocation = Bot.mainFilePath + "CharacterBox/AttackBox/SpellsOriginal.json";
    public static final String fileOutLocation = Bot.mainFilePath + "CharacterBox/AttackBox/SpellsFirstFormatWithType"
            + ".json";


    public SpellsFormatter(JsonArray rawData) {
        final File fileOut = new File(fileOutLocation);
        PrintStream printStream;
        try {
            fileOut.createNewFile();
            printStream = new PrintStream(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        printStream.println("{");
        printStream.println("\"spells\": [");

        Iterator<JsonElement> iterator = rawData.iterator();
        while (iterator.hasNext()) {
            final JsonObject object = (JsonObject) iterator.next();
            final String components = object.get("components").getAsString();
            final String materials;
            if (components.contains("M")) {
                materials = object.get("material").getAsString();
            }
            else {
                materials = "";
            }

            printStream.print(String.format(
                    "{\n"
                            + "\"name\": %s,\n"
                            + "\"description\": %s,\n"
                            + "\"page\": %s,\n"
                            + "\"range\": %s,\n"
                            + "\"components\": %s,\n"
                            + "\"materials\": %s,\n"
                            + "\"ritual\": %s,\n"
                            + "\"duration\": %s,\n"
                            + "\"concentration\": %s,\n"
                            + "\"castTime\": %s,\n"
                            + "\"level\": %s,\n"
                            + "\"school\": %s,\n"
                            + "\"class\": %s,\n"
                            + "\"spellType\": \"\"\n"
                            + "}"
                    ,
                    "\"" + object.get("name").getAsString() + "\"",
                    "\"" + object.get("desc").getAsString().replaceAll("\"", "'") + "\"",
                    getPages(object.get("page").getAsString()),
                    getRange(object.get("range").getAsString()),
                    toList(components),
                    "\"" + materials + "\"",
                    yesNoToTrueFalse(object.get("ritual").getAsString()),
                    getTime(object.get("duration").getAsString()),
                    yesNoToTrueFalse(object.get("concentration").getAsString()),
                    getTime(object.get("casting_time").getAsString()),
                    getLevel(object.get("level").getAsString()),
                    "\"" + object.get("school").getAsString() + "\"",
                    toList(object.get("class").getAsString())
            ));

            if (iterator.hasNext()) {
                printStream.print(",");
            }
            printStream.print("\n");
        }


        printStream.println("]\n");
        printStream.println("}");
    }


    private String getPages(String page) {
        for (int i = page.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(page.charAt(i))) {
                int pageNumber = Integer.parseInt(page.substring(i + 1));
                String book = page.substring(0, i);

                return String.format("{\n\"book\": \"%s\",\n\"pageNumber\": %d\n}", book, pageNumber);
            }
        }

        throw new IllegalArgumentException("Page format incorrect");
    }


    private String getRange(String range) {
        String type;
        int distance;
        if (range.trim().contains(" feet")) {
            type = "distance";
            distance = Integer.parseInt(range.split(" ")[0]);
        }
        else {
            type = range;
            distance = 0;
        }

        return String.format("{\n\"type\": \"%s\",\n\"distance\": %d\n}", type, distance);
    }


    private String toList(String list) {
        String string = "[";

        if (list.contains(",")) {
            boolean first = true;
            for (String component : list.split(", ")) {
                if (!first) {
                    string += ", \"" + component + "\"";
                }
                else {
                    int comma = list.indexOf(",");
                    string += "\"" + list.substring(0, comma) + "\"";
                    first = false;
                }
            }
        }
        else {
            string += "\"" + list + "\"";
        }

        return string + "]";
    }


    private String yesNoToTrueFalse(String yesNo) {
        switch (yesNo) {
            case "yes":
                return "true";
            case "no":
                return "false";
            default:
                throw new IllegalArgumentException("Not yes/no");
        }
    }


    private String getTime(String time) {
        if (time.contains("/1 hour")) {
            time = "1 hour";
        }
        else if (time.contains("Up to")) {
            time = time.substring(6);
        }

        String type = "";
        int timeInt;

        if (time.contains("Special")) {
            type = "special";
        }
        else if (time.contains("dispel")) {
            type = time.replaceAll(" ", "");
        }
        else if (time.contains("bonus")) {
            type = "bonus";
        }
        else if (time.contains("reaction")) {
            type = "reaction";
        }
        else if (time.contains("Instan")) {
            type = "instantaneous";
        }

        if (!type.equals("")) {
            timeInt = 0;
        }
        else {
            timeInt = Integer.parseInt(time.split(" ")[0]);
            if (time.contains("day")) {
                type = "days";
            }
            else if (time.contains("hour")) {
                type = "hours";
            }
            else if (time.contains("minute")) {
                type = "minutes";
            }
            else if (time.contains("round")) {
                type = "rounds";
            }
        }

        return String.format("{\n\"type\": \"%s\",\n\"number\": %d\n}", type, timeInt);
    }


    private String getLevel(String level) {
        if (level.startsWith("C")) {
            return "0";
        }
        else {
            return level.substring(0, 1);
        }
    }


    public static void main(String[] args) {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder
                    .registerTypeAdapter(SpellsFormatter.class, new SpellsFormatter.SpellsDeserializer())
                    .create();

            final String grogJson = new String(readAllBytes(Paths.get(fileInLocation)));
            gson.fromJson(grogJson, SpellsFormatter.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class SpellsDeserializer implements JsonDeserializer<SpellsFormatter> {
        public SpellsFormatter deserialize(JsonElement json, Type typeOfT,
                                           JsonDeserializationContext context) throws JsonParseException
        {
            return new SpellsFormatter(json.getAsJsonObject().get("spells").getAsJsonArray());
        }
    }
}
