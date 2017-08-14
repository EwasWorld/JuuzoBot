package main.java.CharacterBox.AttackBox;

import com.google.gson.*;
import main.java.Foo.IDs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;



public class Weapons {
    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/AttackBox/Weapons.json";



    public enum WeaponsEnum {
        IMPROVISED, SHORTSWORD, LONGSWORD, SHORTBOW, LONGBOW, RAPIER, GREATAXE, MACE, CROSSBOW, LIGHTCROSSBOW
    }



    // TODO: Finesse - use highest of str or dex
    public enum AttackTypeEnum {
        RANGE, MELEE, FINESSE
    }



    public enum DamageType {BLUDGEONING, PIERCING, SLASHING}



    private static Map<WeaponsEnum, Weapon> weapons;


    private Weapons(Object rawObj) {
        weapons = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;
            final WeaponsEnum weapon = WeaponsEnum
                    .valueOf(object.get("name").getAsString().replace(" ", "").toUpperCase());
            final JsonObject damage = object.getAsJsonObject("damage");

            weapons.put(weapon, new Weapon(
                    AttackTypeEnum.valueOf(object.get("attackType").getAsString().toUpperCase()),
                    damage.get("quantity").getAsInt(),
                    damage.get("die").getAsInt(),
                    getStringArrayFromJsonArray(object.getAsJsonArray("attackLines")),
                    getStringArrayFromJsonArray(object.getAsJsonArray("hitLines")),
                    getStringArrayFromJsonArray(object.getAsJsonArray("missLines"))
            ));
        }
    }


    private String[] getStringArrayFromJsonArray(JsonArray jsonArray) {
        List<String> stringsArrayList = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            stringsArrayList.add(element.getAsString());
        }

        return stringsArrayList.toArray(new String[stringsArrayList.size()]);
    }


    public static Weapon getWeaponInfo(WeaponsEnum weapon) {
        if (weapons == null) {
            getWeaponsFromFile();
        }
        return weapons.get(weapon);
    }


    private static void getWeaponsFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Weapons.class, new WeaponsDeserializer()).create();

            final String weaponsJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(weaponsJSON, Weapons.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class WeaponsDeserializer implements JsonDeserializer<Weapons> {
        public Weapons deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException
        {
            return new Weapons(json.getAsJsonObject().get("weapons").getAsJsonArray());
        }
    }
}
