package CharacterBox.BroadInfo;

import CoreBox.Bot;
import CoreBox.Roll;
import ExceptionsBox.BadStateException;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllBytes;



public class Trinkets {
    private static final String fileLocation = Bot.mainFilePath + "CharacterBox/BroadInfo/Trinkets.json";
    private static List<String> trinkets;


    private Trinkets(JsonArray effectsJsonArray) {
        trinkets = new ArrayList<>();
        for (JsonElement element : effectsJsonArray) {
            trinkets.add(element.getAsString());
        }
    }


    public static String getTrinket(String author) {
        try {
            return author + " comes across " + getTrinketLowerCaseStart();
        } catch (NullPointerException e) {
            throw new BadStateException("Trinkets seem to be broken right now");
        }
    }


    public static String getTrinketLowerCaseStart() {
        try {
            initialiseTrinkets();
            String trinket = trinkets.get(Roll.quickRoll(trinkets.size()) - 1);
            // Change the first letter to lower case
            return String.valueOf(trinket.charAt(0)).toLowerCase() + trinket.substring(1);
        } catch (FileNotFoundException e) {
            throw new BadStateException("Trinkets seem to be broken right now");
        }
    }


    private static void initialiseTrinkets() throws FileNotFoundException {
        if (trinkets == null) {
            try {
                final GsonBuilder gsonBuilder = new GsonBuilder();
                final Gson gson = gsonBuilder.registerTypeAdapter(Trinkets.class, new TrinketsDeserializer())
                        .create();

                final String grogJson = new String(readAllBytes(Paths.get(fileLocation)));
                gson.fromJson(grogJson, Trinkets.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static class TrinketsDeserializer implements JsonDeserializer<Trinkets> {
        public Trinkets deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException
        {
            return new Trinkets(json.getAsJsonObject().get("trinkets").getAsJsonArray());
        }
    }
}
