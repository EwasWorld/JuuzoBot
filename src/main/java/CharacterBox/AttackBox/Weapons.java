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
    private static Map<WeaponsEnum, Weapon> weapons;


    // TODO: Breath Weapons

    public enum WeaponProficiency {
        SIMPLEMELEE, SIMPLERANGE, MARTIALMELEE, MARTIALRANGE
    }


    public enum WeaponsEnum {
        IMPROVISED, SHORTSWORD, LONGSWORD, SHORTBOW, LONGBOW, RAPIER, GREATAXE, MACE, CROSSBOW, LIGHTCROSSBOW, UNARMED,
        LIGHTHAMMER;

        private WeaponProficiency weaponProficiency;

        static {
            IMPROVISED.weaponProficiency = WeaponProficiency.SIMPLEMELEE;
            MACE.weaponProficiency = WeaponProficiency.SIMPLEMELEE;
            LIGHTHAMMER.weaponProficiency = WeaponProficiency.SIMPLEMELEE;
            UNARMED.weaponProficiency = WeaponProficiency.SIMPLEMELEE;

            SHORTSWORD.weaponProficiency = WeaponProficiency.MARTIALMELEE;
            RAPIER.weaponProficiency = WeaponProficiency.MARTIALMELEE;
            GREATAXE.weaponProficiency = WeaponProficiency.MARTIALMELEE;
            LONGSWORD.weaponProficiency = WeaponProficiency.MARTIALMELEE;

            SHORTBOW.weaponProficiency = WeaponProficiency.SIMPLERANGE;
            LIGHTCROSSBOW.weaponProficiency = WeaponProficiency.SIMPLERANGE;

            LONGBOW.weaponProficiency = WeaponProficiency.MARTIALRANGE;
            CROSSBOW.weaponProficiency = WeaponProficiency.MARTIALRANGE;
        }
    }



    public enum DamageType {BLUDGEONING, PIERCING, SLASHING}





    private Weapons(Object rawObj) {
        weapons = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;
            final WeaponsEnum weapon = WeaponsEnum
                    .valueOf(object.get("name").getAsString().replace(" ", "").toUpperCase());
            final JsonObject damage = object.getAsJsonObject("damage");

            weapons.put(weapon, new Weapon(
                    Weapon.AttackTypeEnum.valueOf(object.get("attackType").getAsString().toUpperCase()),
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


    public static String getWeaponsList() {
        String weapons = "Available weapons: ";
        WeaponsEnum[] weaponsEnums = WeaponsEnum.values();

        for (int i = 0; i < weaponsEnums.length; i++) {
            weapons += weaponsEnums[i].toString();

            if (i < weaponsEnums.length - 1) {
                weapons += ", ";
            }
        }

        return weapons;
    }


    private static class WeaponsDeserializer implements JsonDeserializer<Weapons> {
        public Weapons deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException
        {
            return new Weapons(json.getAsJsonObject().get("weapons").getAsJsonArray());
        }
    }
}
