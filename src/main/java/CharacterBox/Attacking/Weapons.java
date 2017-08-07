package main.java.CharacterBox.Attacking;

import com.google.gson.*;
import main.java.CharacterBox.AbilitySkillConstants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;



public class Weapons {
    private static final String fileLocation = "src/main/java/CharacterBox/Attacking/Weapons.json";



    public enum WeaponsEnum {
        SHORTSWORD, SHORTBOW, RAPIER;
    }



    public enum AttackTypeEnum {
        RANGE, MELEE, FINESSE;

        private AbilitySkillConstants.AbilityEnum modifier;


        static {
            RANGE.modifier = AbilitySkillConstants.AbilityEnum.DEXTERITY;
            FINESSE.modifier = AbilitySkillConstants.AbilityEnum.DEXTERITY;
            MELEE.modifier = AbilitySkillConstants.AbilityEnum.STRENGTH;
        }


        public AbilitySkillConstants.AbilityEnum getModifier() {
            return modifier;
        }
    }



    public enum WeaponTypeEnum {
        BOW, SWORD, RAPIER;

        private AttackTypeEnum attackType;


        static {
            BOW.attackType = AttackTypeEnum.RANGE;
            SWORD.attackType = AttackTypeEnum.MELEE;
            RAPIER.attackType = AttackTypeEnum.FINESSE;
        }


        public AttackTypeEnum getAttackType() {
            return attackType;
        }
    }



    private static Map<WeaponsEnum, Weapon> weapons;


    private Weapons(Object rawObj) {
        weapons = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;
            final WeaponsEnum weapon = WeaponsEnum
                    .valueOf(object.get("name").getAsString().replace(" ", "").toUpperCase());

            weapons.put(weapon, new Weapon(
                    WeaponTypeEnum.valueOf(object.get("weaponType").getAsString().toUpperCase()),
                    getStringArrayFromJsonArray(object.getAsJsonArray("attackLines")),
                    getStringArrayFromJsonArray(object.getAsJsonArray("hitLines")),
                    getStringArrayFromJsonArray(object.getAsJsonArray("missLines"))
            ));
        }
    }


    private String[] getStringArrayFromJsonArray(JsonArray array) {
        List<String> stringsArrayList = new ArrayList<>();
        for (JsonElement element : array) {
            stringsArrayList.add(element.getAsString());
        }
        return (String[]) stringsArrayList.toArray();
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
