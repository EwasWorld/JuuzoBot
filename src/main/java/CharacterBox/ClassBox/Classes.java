package main.java.CharacterBox.ClassBox;


import com.google.gson.*;
import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.AttackBox.Weapons;
import main.java.Foo.IDs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



/*
 * Class information used for setting up a character
 */
public class Classes {
    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/ClassBox/Classes.json";
    private static Map<ClassEnum, Class_> classes;



    public enum ClassEnum {
        BARBARIAN, BARD, CLERIC, DRUID, FINESSEFIGHTER,
        FIGHTER, MONK, PALADIN, RANGER, ROGUE,
        SORCERER, WARLOCK, WIZARD
    }


    private Classes(Object rawObj) {
        classes = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            final ClassEnum classEnum = ClassEnum.valueOf(object.get("name").getAsString().toUpperCase());
            final JsonObject funds = object.getAsJsonObject("funds");
            classes.put(classEnum, new Class_(
                    object.get("hitDie").getAsInt(),
                    createAbilityOrder(object.getAsJsonObject("abilityOrder")),
                    createSavingThrows(object.getAsJsonArray("savingThrows")),
                    object.get("skillProficienciesQuantity").getAsInt(),
                    createSkillProficiencies(object.getAsJsonArray("skillProficiencies")),
                    new Funds(
                            funds.get("quantity").getAsInt(),
                            funds.get("multiply").getAsBoolean()
                    ),
                    Weapons.WeaponsEnum.valueOf(object.get("startWeapon").getAsString().toUpperCase())
            ));
        }
    }


    private static AbilitySkillConstants.AbilityEnum[] createAbilityOrder(JsonObject abilityOrderObj) {
        final AbilitySkillConstants.AbilityEnum[] newAbilityOrder = new AbilitySkillConstants.AbilityEnum[6];
        newAbilityOrder[abilityOrderObj.get("str").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.STRENGTH;
        newAbilityOrder[abilityOrderObj.get("dex").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.DEXTERITY;
        newAbilityOrder[abilityOrderObj.get("con").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.CONSTITUTION;
        newAbilityOrder[abilityOrderObj.get("int").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.INTELLIGENCE;
        newAbilityOrder[abilityOrderObj.get("wis").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.WISDOM;
        newAbilityOrder[abilityOrderObj.get("cha").getAsInt() - 1]
                = AbilitySkillConstants.AbilityEnum.CHARISMA;
        return newAbilityOrder;
    }


    private static Set<AbilitySkillConstants.AbilityEnum> createSavingThrows(JsonArray savingThrowsArray) {
        final Set<AbilitySkillConstants.AbilityEnum> savingThrows = new HashSet<>();
        for (JsonElement savingThrow : savingThrowsArray) {
            savingThrows.add(AbilitySkillConstants.AbilityEnum.valueOf(savingThrow.getAsString().toUpperCase()));
        }
        return savingThrows;
    }


    private static Set<AbilitySkillConstants.SkillEnum> createSkillProficiencies(JsonArray skillProficienciesArray) {
        final Set<AbilitySkillConstants.SkillEnum> skillProficiencies = new HashSet<>();
        for (JsonElement skillProficiency : skillProficienciesArray) {
            skillProficiencies
                    .add(AbilitySkillConstants.SkillEnum.valueOf(skillProficiency.getAsString().toUpperCase()));
        }
        return skillProficiencies;
    }


    public static Class_ getClassInfo(ClassEnum classEnum) {
        if (classes == null) {
            getClassesFromFile();
        }
        return classes.get(classEnum);
    }


    private static void getClassesFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Classes.class, new ClassSetUpDeserializer()).create();

            final String classSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(classSetUpJSON, Classes.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getClassesList() {
        String classes = "Available classes: ";
        ClassEnum[] classEnums = ClassEnum.values();

        for (int i = 0; i < classEnums.length; i++) {
            classes += classEnums[i].toString();

            if (i < classEnums.length - 1) {
                classes += ", ";
            }
        }

        return classes;
    }


    private static class ClassSetUpDeserializer implements JsonDeserializer<Classes> {
        public Classes deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException
        {
            return new Classes(json.getAsJsonObject().get("classes").getAsJsonArray());
        }
    }
}
