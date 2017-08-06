package main.java.CharacterBox.RaceBox;

import com.google.gson.*;
import main.java.CharacterBox.ClassBox.Classes;
import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.CharacterConstants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



/*
 * Used to import races from JSON file
 */
public class Races {
    private static final String fileLocation = "src/main/java/RaceBox/Races.json";
    private static Map<RaceEnum, Race> races;



    public enum RaceEnum {
        DWARF, ELF,
        HALFLING, HUMAN,
        DRAGONBORN, GNOME,
        HALFELF, HALFORC,
        TIEFLING
    }


    public Races(Object rawObj) {
        races = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            final RaceEnum raceEnum = RaceEnum.valueOf(object.get("name").getAsString().toUpperCase());

            races.put(raceEnum, new Race(
                    object.get("subrace").getAsString(),
                    createAbilityIncreases(object.getAsJsonObject("abilityIncreases")),
                    object.get("ageUpperBound").getAsInt(),
                    CharacterConstants.Size.valueOf(object.get("size").getAsString().toUpperCase()),
                    object.get("speed").getAsInt(),
                    createLanguages(object.getAsJsonArray("languages"))
            ));
        }
    }


    private static Map<AbilitySkillConstants.AbilityEnum, Integer> createAbilityIncreases(JsonObject abilityIncreases) {
        Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreasesMap = new HashMap<>();
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.STRENGTH, abilityIncreases.get("str").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.DEXTERITY, abilityIncreases.get("dex").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CONSTITUTION, abilityIncreases.get("con").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.INTELLIGENCE, abilityIncreases.get("int").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.WISDOM, abilityIncreases.get("wis").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CHARISMA, abilityIncreases.get("cha").getAsInt());
        return abilityIncreasesMap;
    }


    /*
     * If there is a wildcard language then it picks one at random
     */
    private static Set<CharacterConstants.Language> createLanguages(JsonArray languagesJson) throws IllegalArgumentException {
        Set<CharacterConstants.Language> languages = new HashSet<>();
        for (JsonElement language : languagesJson) {
            final String languageString = language.getAsString().toUpperCase();
            if (!languageString.equals("WILDCARD")) {
                languages.add(CharacterConstants.Language.valueOf(languageString));
            }
            else {
                CharacterConstants.Language[] allLanguages = CharacterConstants.Language.values();
                languages.add(allLanguages[new Random().nextInt(allLanguages.length)]);
            }
        }
        return languages;
    }


    public static Race getRaceInfo(RaceEnum raceEnum) {
        if (races == null) {
            getRacesFromFile();
        }
        return races.get(raceEnum);
    }


    private static void getRacesFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Classes.class, new RaceSetUpDeserializer()).create();

            final String raceSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(raceSetUpJSON, Races.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class RaceSetUpDeserializer implements JsonDeserializer<Races> {
        public Races deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException
        {
            return new Races(json.getAsJsonObject().get("races").getAsJsonArray());
        }
    }
}
