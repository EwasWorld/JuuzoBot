package CharacterBox.BroardInfo;

import CharacterBox.Abilities;
import CharacterBox.CharacterConstants;
import Foo.BadUserInputException;
import Foo.IDs;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



/*
 * Class information used for setting up a character
 */
public class Race {
    public enum RaceEnum {DWARF, ELF, HALFLING, HUMAN, DRAGONBORN, GNOME, HALFELF, HALFORC, TIEFLING}



    private static final String fileLocation = IDs.mainFilePath + "CharacterBox/BroardInfo/Races.json";
    private static Map<RaceEnum, Race> races;
    private static Map<SubRace.SubRaceEnum, SubRace> subRaces;
    private Abilities abilityIncreases;
    private int ageLowerBound;
    private int ageUpperBound;
    private CharacterConstants.Size size;
    private int speed;
    private Set<CharacterConstants.Language> languages;


    protected Race() { }


    /*
     * Create races and subraces maps
     */
    public Race(Object rawObj) {
        final JsonObject mainObject = (JsonObject) rawObj;
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
        final Map<CharacterConstants.AbilityEnum, Integer> abilityIncreasesMap = new HashMap<>();
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.STRENGTH, abilityIncreases.get("str").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.DEXTERITY, abilityIncreases.get("dex").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.CONSTITUTION, abilityIncreases.get("con").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.INTELLIGENCE, abilityIncreases.get("int").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.WISDOM, abilityIncreases.get("wis").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.CHARISMA, abilityIncreases.get("cha").getAsInt());
        return new Abilities(abilityIncreasesMap);
    }


    /*
     * If there is a wildcard language then it picks one at random
     */
    private static Set<CharacterConstants.Language> createLanguages(JsonArray languagesJson) {
        final Set<CharacterConstants.Language> languages = new HashSet<>();
        for (JsonElement language : languagesJson) {
            languages.add(CharacterConstants.Language.valueOf(language.getAsString().toUpperCase()));
        }
        return languages;
    }


    private Race(Abilities abilityIncreases, int ageLowerBound, int ageUpperBound,
                 CharacterConstants.Size size, int speed, Set<CharacterConstants.Language> languages)
    {
        if (ageLowerBound >= ageUpperBound) {
            throw new BadUserInputException("Lower bound age must be larger than upper bound");
        }

        this.abilityIncreases = abilityIncreases;
        this.ageLowerBound = ageLowerBound;
        this.ageUpperBound = ageUpperBound;
        this.size = size;
        this.speed = speed;
        this.languages = languages;
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
            final Gson gson = gsonBuilder.registerTypeAdapter(Race.class, new RaceSetUpDeserializer()).create();

            final String raceSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(raceSetUpJSON, Race.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static SubRace getRaceInfo(SubRace.SubRaceEnum subRaceEnum) {
        if (subRaces == null) {
            getRacesFromFile();
        }
        return subRaces.get(subRaceEnum);
    }


    public static String getRacesList() {
        if (races == null) {
            getRacesFromFile();
        }

        String races = "Available races: ";
        final RaceEnum[] raceEnums = RaceEnum.values();

        for (int i = 0; i < raceEnums.length; i++) {
            races += raceEnums[i].toString();

            if (i < raceEnums.length - 1) {
                races += ", ";
            }
        }

        races += "\n\nSubraces: ";
        final Iterator<SubRace.SubRaceEnum> subRaceIterator = subRaces.keySet().iterator();
        while (subRaceIterator.hasNext()) {
            SubRace.SubRaceEnum subRaceEnum = subRaceIterator.next();
            races += subRaceEnum.toString() + " " + subRaces.get(subRaceEnum).getMainRace();

            if (subRaceIterator.hasNext()) {
                races += ", ";
            }
        }

        return races;
    }


    public int getAbilityIncreases(CharacterConstants.AbilityEnum ability) {
        return abilityIncreases.getStat(ability);
    }


    public int generateRandomAge() {
        return new Random().nextInt(ageUpperBound - ageLowerBound) + ageLowerBound;
    }


    public CharacterConstants.Size getSize() {
        return size;
    }


    public int getSpeed() {
        return speed;
    }


    public Set<CharacterConstants.Language> getLanguages() {
        return languages;
    }


    private static class RaceSetUpDeserializer implements JsonDeserializer<Race> {
        public Race deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException
        {
            return new Race(json.getAsJsonObject().get("races"));
        }
    }
}
