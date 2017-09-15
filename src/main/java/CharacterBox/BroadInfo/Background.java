package CharacterBox.BroadInfo;

import CharacterBox.Alignment;
import CharacterBox.CharacterConstants;
import CoreBox.Bot;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.readAllBytes;



public class Background {
    public enum BackgroundEnum {
        ACOLYTE, CHARLATAN, CRIMINAL, ENTERTAINER, FOLKHERO, GUILDARTISAN,
        HERMIT, NOBLE, OUTLANDER, SAGE, SAILOR, SOLDIER, URCHIN
    }



    public enum InstrumentEnum {
        BAGPIPES("Bagpipes"), DRUM("Drum"), DULCIMER("Dulcimer"), FLUTE("Flute"),
        LUTE("Lute"), LYRE("Lyre"), HORN("Horn"), PANFLUTE("Pan Flute"),
        SHAWM("Shawm"), VIOL("Viol"), Castanets("Castanets"), Didgeridoo("Digeridoo"),
        Euphonium("Euphonium"), HurdyGurdy("HurdyGurdy"), Violin("Violin");

        private String name;


        InstrumentEnum(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }



    private static final String fileLocation = Bot.getMainFilePath() + "CharacterBox/BroadInfo/Backgrounds.json";
    private static Map<String, Background> backgrounds;
    private String[] possibilities;
    private Set<CharacterConstants.SkillEnum> proficiencies;
    private Set<CharacterConstants.Language> languages;
    // TODO: Tools
    private String[] traits;
    private Ideal[] ideals;
    private String[] bonds;
    private String[] flaws;


    public Background(Object rawObj) {
        backgrounds = new HashMap<>();

        for (JsonElement element : (JsonArray) rawObj) {
            final JsonObject object = (JsonObject) element;

            backgrounds.put(
                    object.get("name").getAsString().toUpperCase(),
                    new Background(
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("possibilities")),
                            CharacterConstants.createSkillProficiencies(object.getAsJsonArray("proficiencies")),
                            CharacterConstants.createLanguages(object.getAsJsonArray("languages")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("traits")),
                            createIdeals(object.getAsJsonArray("ideals")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("bonds")),
                            CharacterConstants.getStringArrayFromJsonArray(object.getAsJsonArray("flaws"))
                    )
            );
        }
    }


    private Ideal[] createIdeals(JsonArray idealsJsonArray) {
        List<Ideal> ideals = new ArrayList<>();
        for (JsonElement element : idealsJsonArray) {
            final JsonObject object = (JsonObject) element;
            ideals.add(new Ideal(
                    object.get("name").getAsString(),
                    object.get("description").getAsString(),
                    object.get("alignment").getAsString()
            ));
        }
        return ideals.toArray(new Ideal[ideals.size()]);
    }


    private Background(String[] possibilities, Set<CharacterConstants.SkillEnum> proficiencies,
                       Set<CharacterConstants.Language> languages, String[] traits,
                       Ideal[] ideals, String[] bonds, String[] flaws)
    {
        this.possibilities = possibilities;
        this.proficiencies = proficiencies;
        this.languages = languages;
        this.traits = traits;
        this.ideals = ideals;
        this.bonds = bonds;
        this.flaws = flaws;
    }


    public static Background getBackgroundInfo(String background) {
        if (backgrounds == null) {
            getBackgroundsFromFile();
        }
        background = background.toUpperCase();
        if (backgrounds.containsKey(background)) {
            return backgrounds.get(background);
        }
        else {
            throw new BadUserInputException("Invalid background");
        }
    }


    private static void getBackgroundsFromFile() {
        try {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            final Gson gson = gsonBuilder.registerTypeAdapter(Background.class, new BackgroundsSetUpDeserializer())
                    .create();

            final String backgoundsSetUpJSON = new String(readAllBytes(Paths.get(fileLocation)));
            gson.fromJson(backgoundsSetUpJSON, Background.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Returns whether the background given exists
     */
    public static boolean contains(String background) {
        if (backgrounds == null) {
            getBackgroundsFromFile();
        }
        return backgrounds.containsKey(background.toUpperCase());
    }


    public static String getBackgroundsList() {
        if (backgrounds == null) {
            getBackgroundsFromFile();
        }

        String backgroundsList = "Available backgrounds: ";
        final String[] backgroundsArr = backgrounds.keySet().toArray(new String[backgrounds.size()]);

        for (int i = 0; i < backgroundsArr.length; i++) {
            backgroundsList += backgroundsArr[i];

            if (i < backgroundsArr.length - 1) {
                backgroundsList += ", ";
            }
        }

        return backgroundsList;
    }


    public String getRandomPossibility() {
        return possibilities[new Random().nextInt(possibilities.length)];
    }


    public String getRandomTrait() {
        return traits[new Random().nextInt(traits.length)];
    }


    /*
     * Returns a random ideal ensuring the the alignment given allows for the ideal
     */
    public Ideal getRandomIdeal(Alignment alignment) {
        final List<Ideal> ideals = new ArrayList<>();
        for (Ideal ideal : this.ideals) {
            if (ideal.alignment.checkMatches(alignment)) {
                ideals.add(ideal);
            }
        }
        return ideals.get(new Random().nextInt(ideals.size()));
    }


    public String getRandomBond() {
        return bonds[new Random().nextInt(bonds.length)];
    }


    public String getRandomFlaw() {
        return flaws[new Random().nextInt(flaws.length)];
    }


    public Set<CharacterConstants.SkillEnum> getProficiencies() {
        return proficiencies;
    }


    public Set<CharacterConstants.Language> getLanguages() {
        return languages;
    }


    private static class BackgroundsSetUpDeserializer implements JsonDeserializer<Background> {
        public Background deserialize(JsonElement json, Type typeOfT,
                                      JsonDeserializationContext context) throws JsonParseException
        {
            return new Background(json.getAsJsonObject().get("backgrounds").getAsJsonArray());
        }
    }



    public class Ideal {
        private String name;
        private String description;
        private Alignment alignment;


        private Ideal(String name, String description, String alignment) {
            this.name = name;
            this.description = description;

            alignment = alignment.toUpperCase();
            if (alignment.equals("ANY")) {
                this.alignment = new Alignment();
            }
            else {
                try {
                    this.alignment = new Alignment(Alignment.GoodEvilEnum.valueOf(alignment));
                } catch (IllegalArgumentException e) {
                    try {
                        this.alignment = new Alignment(Alignment.LawChaosEnum.valueOf(alignment));
                    } catch (IllegalArgumentException e1) {
                        throw new BadStateException("Incorrect alignment for ideal");
                    }
                }
            }
        }


        public String getName() {
            return name;
        }


        public String getDescription() {
            return description;
        }
    }
}
