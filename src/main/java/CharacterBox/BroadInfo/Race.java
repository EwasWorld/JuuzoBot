package CharacterBox.BroadInfo;

import CharacterBox.Alignment;
import CharacterBox.CharacterConstants;
import CoreBox.Bot;
import ExceptionsBox.BadUserInputException;
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
    public enum RaceEnum {
        DWARF, ELF, HALFLING, HUMAN, DRAGONBORN, GNOME, HALFELF, HALFORC, TIEFLING;


        public String toString() {
            String enumStr = super.toString();
            enumStr = enumStr.charAt(0) + enumStr.substring(1).toLowerCase();
            if (enumStr.startsWith("Half")) {
                enumStr = enumStr.substring(0, 4) + "-" + String.valueOf(enumStr.charAt(4)).toUpperCase() + enumStr
                        .substring(5);
            }
            return enumStr;
        }
    }



    private static final String fileLocation = Bot.getResourceFilePath() + "CharacterGeneration/Races.json";
    private static Map<RaceEnum, Race> races;
    private static Map<SubRace.SubRaceEnum, SubRace> subRaces;
    private Map<CharacterConstants.AbilityEnum, Integer> abilityIncreases;
    private int ageLowerBound;
    private int ageUpperBound;
    private CharacterConstants.Size size;
    private int speed;
    private Set<CharacterConstants.Language> languages;
    private List<Alignment.GoodEvilEnum> goodEvilEnums;
    private List<Alignment.LawChaosEnum> lawChaosEnums;


    protected Race() {
    }


    /*
     * Create races and subraces maps
     */
    public Race(Object rawObj) {
        final JsonObject mainObject = (JsonObject) rawObj;
        races = new HashMap<>();
        for (JsonElement element : mainObject.getAsJsonArray("mainraces")) {
            final JsonObject object = (JsonObject) element;
            final RaceEnum raceEnum = RaceEnum.valueOf(object.get("name").getAsString().toUpperCase());
            final JsonObject alignments = object.getAsJsonObject("alignment");
            races.put(raceEnum, new Race(
                    createAbilityIncreases(object.getAsJsonObject("abilityIncreases")),
                    object.get("ageLowerBound").getAsInt(),
                    object.get("ageUpperBound").getAsInt(),
                    CharacterConstants.Size.valueOf(object.get("size").getAsString().toUpperCase()),
                    object.get("speed").getAsInt(),
                    CharacterConstants.createLanguages(object.getAsJsonArray("languages")),
                    createGoodEvilAlignments(alignments.getAsJsonArray("goodEvil")),
                    createLawChaosAlignments(alignments.getAsJsonArray("lawChaos"))
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


    private static Map<CharacterConstants.AbilityEnum, Integer> createAbilityIncreases(JsonObject abilityIncreases) {
        final Map<CharacterConstants.AbilityEnum, Integer> abilityIncreasesMap = new HashMap<>();
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.STRENGTH, abilityIncreases.get("str").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.DEXTERITY, abilityIncreases.get("dex").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.CONSTITUTION, abilityIncreases.get("con").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.INTELLIGENCE, abilityIncreases.get("int").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.WISDOM, abilityIncreases.get("wis").getAsInt());
        abilityIncreasesMap.put(CharacterConstants.AbilityEnum.CHARISMA, abilityIncreases.get("cha").getAsInt());
        return abilityIncreasesMap;
    }


    private static List<Alignment.GoodEvilEnum> createGoodEvilAlignments(JsonArray alignments) {
        final List<Alignment.GoodEvilEnum> goodEvils = new ArrayList<>();
        for (JsonElement element : alignments) {
            goodEvils.add(Alignment.GoodEvilEnum.valueOf(element.getAsString().toUpperCase()));
        }
        return goodEvils;
    }


    private static List<Alignment.LawChaosEnum> createLawChaosAlignments(JsonArray lawChaos) {
        final List<Alignment.LawChaosEnum> lawChaoses = new ArrayList<>();
        for (JsonElement element : lawChaos) {
            lawChaoses.add(Alignment.LawChaosEnum.valueOf(element.getAsString().toUpperCase()));
        }
        return lawChaoses;
    }


    private Race(Map<CharacterConstants.AbilityEnum, Integer> abilityIncreases, int ageLowerBound, int ageUpperBound,
                 CharacterConstants.Size size, int speed, Set<CharacterConstants.Language> languages,
                 List<Alignment.GoodEvilEnum> goodEvilEnums, List<Alignment.LawChaosEnum> lawChaosEnums)
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
        this.goodEvilEnums = goodEvilEnums;
        this.lawChaosEnums = lawChaosEnums;
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


    public Map<CharacterConstants.AbilityEnum, Integer> getAbilityIncreases() {
        return abilityIncreases;
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


    public Alignment getRandomAlignment() {
        return new Alignment(
                lawChaosEnums.get(new Random().nextInt(lawChaosEnums.size())),
                goodEvilEnums.get(new Random().nextInt(lawChaosEnums.size()))
        );
    }


    private static class RaceSetUpDeserializer implements JsonDeserializer<Race> {
        public Race deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException
        {
            return new Race(json.getAsJsonObject().get("races"));
        }
    }
}
