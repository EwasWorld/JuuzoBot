package CoreBox;

import ExceptionsBox.BadStateException;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllBytes;



/*
 * Users drink a grog potion which has a random effect on them
 */
public class GrogList {
    private static final String fileLocation = Bot.getResourceFilePath() + "GrogEffects.json";
    private static List<String> effects;


    /*
     * Initialises the list of effects
     */
    private GrogList(JsonArray effectsJsonArray) {
        effects = new ArrayList<>();
        for (JsonElement element : effectsJsonArray) {
            effects.add(element.getAsString());
        }
    }


    /*
     * Returns a random effect with the author's name substituted in where appropriate
     */
    public static String drinkGrog(String author) {
        try {
            int roll = Die.quickRoll(1000) - 1;
            String effect = GrogList.getEffects().get(roll);

            effect = effect.replaceAll("PC", author);
            effect = effect.replaceAll("the PC", author);
            effect = effect.replaceAll("The PC", author);

            return String.format("%s drinks an Essence of Balthazar potion. %s", author, effect);
        } catch (NullPointerException | FileNotFoundException e) {
            throw new BadStateException("Potions seem to be broken right now");
        }
    }


    /*
     * Used to import the list of effects from a json file
     */
    private static List<String> getEffects() throws FileNotFoundException {
        if (effects == null) {
            try {
                final GsonBuilder gsonBuilder = new GsonBuilder();
                final Gson gson = gsonBuilder.registerTypeAdapter(GrogList.class, new GrogList.GrogDeserializer())
                        .create();

                final String grogJson = new String(readAllBytes(Paths.get(fileLocation)));
                gson.fromJson(grogJson, GrogList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return effects;
    }


    /*
     * Used to import the list of effects from a json file
     */
    private static class GrogDeserializer implements JsonDeserializer<GrogList> {
        public GrogList deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException
        {
            return new GrogList(json.getAsJsonObject().get("effects").getAsJsonArray());
        }
    }
}
