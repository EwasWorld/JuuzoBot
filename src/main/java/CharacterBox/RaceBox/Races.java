package main.java.CharacterBox.RaceBox;

import com.google.gson.*;
import main.java.CharacterBox.AbilitySkillConstants;
import main.java.CharacterBox.Abilities;
import main.java.CharacterBox.CharacterConstants;
import main.java.Foo.IDs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



/*
 * Used to import races from JSON file
 */
public class Races {
    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/RaceBox/Races.json";
    private static Map<RaceEnum, Race> races;
    private static Map<SubRace.SubRaceEnum, SubRace> subRaces;



    public enum RaceEnum {
        DWARF, ELF,
        HALFLING, HUMAN,
        DRAGONBORN, GNOME,
        HALFELF, HALFORC,
        TIEFLING
    }


    public Races(Object rawObj) {
        JsonObject mainObject = (JsonObject) rawObj;
        races = new HashMap<>();
        for (JsonElement element : mainObject.getAsJsonArray("mainraces")) {
            final JsonObject object = (JsonObject) element;
            final RaceEnum raceEnum = RaceEnum.valueOf(object.get("name").getAsString().toUpperCase());
            races.put(raceEnum, new Race(
                    createAbilityIncreases(object.getAsJsonObject("abilityIncreases")),
                    object.get("ageLowerBound").getAsInt(),
                    object.get("ageUpperBound").getAsInt(),
                    CharacterConstants.Size.valueOf(object.get("size").getAsString().toUpperCase()),
                    object.get("speed").getAsInt(),
                    createLanguages(object.getAsJsonArray("languages"))
            ));
        }

        subRaces = new HashMap<>();
        for (JsonElement element : mainObject.getAsJsonArray("subraces")) {
            final JsonObject object = (JsonObject) element;
            final SubRace.SubRaceEnum subRaceEnum = SubRace.SubRaceEnum
                    .valueOf(object.get("name").getAsString().toUpperCase());
            subRaces.put(subRaceEnum, new SubRace(
                    RaceEnum.valueOf(object.get("mainRace").getAsString().toUpperCase()),
                    createAbilityIncreases(object.getAsJsonObject("abilityIncreases"))
            ));
        }
    }


    private static Abilities createAbilityIncreases(JsonObject abilityIncreases) {
        Map<AbilitySkillConstants.AbilityEnum, Integer> abilityIncreasesMap = new HashMap<>();
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.STRENGTH, abilityIncreases.get("str").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.DEXTERITY, abilityIncreases.get("dex").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CONSTITUTION, abilityIncreases.get("con").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.INTELLIGENCE, abilityIncreases.get("int").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.WISDOM, abilityIncreases.get("wis").getAsInt());
        abilityIncreasesMap.put(AbilitySkillConstants.AbilityEnum.CHARISMA, abilityIncreases.get("cha").getAsInt());
        return new Abilities(abilityIncreasesMap);
    }


    /*
     * If there is a wildcard language then it picks one at random
     */
    private static Set<CharacterConstants.Language> createLanguages(JsonArray languagesJson) {
        Set<CharacterConstants.Language> languages = new HashSet<>();
        for (JsonElement language : languagesJson) {
            languages.add(CharacterConstants.Language.valueOf(language.getAsString().toUpperCase()));
        }
        return languages;
    }


    public static Race getRaceInfo(RaceEnum raceEnum) {
        if (races == null) {
            getRacesFromFile();
        }
        return races.get(raceEnum);
    }


    public static SubRace getRaceInfo(SubRace.SubRaceEnum subRaceEnum) {
        if (subRaces == null) {
            getRacesFromFile();
        }
        return subRaces.get(subRaceEnum);
    }


    private static void getRacesFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Races.class, new RaceSetUpDeserializer()).create();

            final String raceSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(raceSetUpJSON, Races.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getRacesList() {
        String races = "Available races: ";
        RaceEnum[] raceEnums = RaceEnum.values();

        for (int i = 0; i < raceEnums.length; i++) {
            races += raceEnums[i].toString();

            if (i < raceEnums.length - 1) {
                races += ", ";
            }
        }

        return races;
    }


    private static class RaceSetUpDeserializer implements JsonDeserializer<Races> {
        public Races deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException
        {
            return new Races(json.getAsJsonObject().get("races"));
        }
    }
}
