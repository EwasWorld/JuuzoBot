package Grog;

import ExceptionsBox.BadStateException;
import Foo.IDs;
import Foo.Roll;
import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.readAllBytes;



public class GrogList {
    private static final String fileLocation = IDs.mainFilePath + "Grog/GrogEffects.json";
    private static List<String> effects;


    private GrogList(JsonArray effectsJsonArray) {
        effects = new ArrayList<>();
        for (JsonElement element : effectsJsonArray) {
            effects.add(element.getAsString());
        }
    }


    public static String drinkGrog(String author) {
        try {
            int roll = Roll.quickRoll(1000) - 1;
            String effect = GrogList.getEffects().get(roll);
            effect = effect.replaceAll("PC", author);
            return author + " drinks an Essence of Balthazar potion. " + effect;
        } catch (NullPointerException | FileNotFoundException e) {
            throw new BadStateException("Potions seem to be broken right now");
        }
    }


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



    private static class GrogDeserializer implements JsonDeserializer<GrogList> {
        public GrogList deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException
        {
            return new GrogList(json.getAsJsonObject().get("effects").getAsJsonArray());
        }
    }
}
